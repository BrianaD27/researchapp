package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.AssociateLink;
import com.vsu.researchapp.domain.model.AssociateProfile;
import com.vsu.researchapp.domain.model.CalendarEntry;
import com.vsu.researchapp.domain.model.Notification;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateProfileRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.CalendarEntryRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.NotificationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ApplicationJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ResearchOpportunityJpaRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Personal calendars for faculty, Research Associates, and admins. Each person
// owns their own calendar and can post whatever they like. PUBLIC entries show
// on their profile and to students; PRIVATE entries are never shown to anyone
// else. Opportunity application deadlines are added automatically.
@Service
public class CalendarService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);
    private static final Set<String> OWNER_ROLES = Set.of("PROFESSOR", "ASSOCIATE", "ADMIN");
    private static final Set<String> COLORS = Set.of("blue", "teal", "purple", "coral", "amber", "gray");
    private static final int MAX_ENTRIES = 500;
    private static final int MAX_RANGE_DAYS = 100;
    private static final int MAX_NOTIFY_PER_DAY = 5;
    private static final int MAX_RECIPIENTS = 500;
    private static final int UPCOMING_DAYS = 21;
    private static final int UPCOMING_LIMIT = 8;

    public record EntryInput(String title, LocalDate startDate, LocalDate endDate, LocalTime startTime,
                             LocalTime endTime, boolean repeatWeekly, LocalDate repeatUntil, String location,
                             String notes, String color, String visibility, boolean notifyStudents,
                             boolean away) {}

    public record EntryView(Long id, String title, LocalDate startDate, LocalDate endDate, LocalTime startTime,
                            LocalTime endTime, boolean repeatWeekly, LocalDate repeatUntil, String location,
                            String notes, String color, String visibility, boolean notified, boolean away) {}

    // One thing on one day: a single entry, one week of a repeating entry, or a deadline.
    public record Occurrence(Long entryId, String kind, String title, LocalDate date, LocalDate endDate,
                             LocalTime startTime, LocalTime endTime, String location, String notes, String color,
                             String visibility, boolean repeatWeekly, Long opportunityId, boolean away) {}

    public record MyCalendar(List<EntryView> entries, List<Occurrence> occurrences, int notifiedStudents) {}

    public record Upcoming(String ownerName, List<Occurrence> items) {}

    private final CalendarEntryRepository entries;
    private final UserAccountRepository accounts;
    private final ProfessorJpaRepository professors;
    private final ResearchOpportunityJpaRepository opportunities;
    private final ApplicationJpaRepository applications;
    private final AssociateLinkRepository links;
    private final AssociateProfileRepository associateProfiles;
    private final NotificationRepository notifications;
    private final CurrentUserService currentUser;

    public CalendarService(CalendarEntryRepository entries, UserAccountRepository accounts,
            ProfessorJpaRepository professors, ResearchOpportunityJpaRepository opportunities,
            ApplicationJpaRepository applications, AssociateLinkRepository links,
            AssociateProfileRepository associateProfiles, NotificationRepository notifications,
            CurrentUserService currentUser) {
        this.entries = entries;
        this.accounts = accounts;
        this.professors = professors;
        this.opportunities = opportunities;
        this.applications = applications;
        this.links = links;
        this.associateProfiles = associateProfiles;
        this.notifications = notifications;
        this.currentUser = currentUser;
    }

    // ---- Your own calendar ----

    @Transactional(readOnly = true)
    public MyCalendar mine(LocalDate from, LocalDate to) {
        UserAccount me = requireOwner();
        if (from == null || to == null || to.isBefore(from)) throw bad("Choose a date range");
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) throw bad("Date range is too long");
        List<CalendarEntry> list = entries.findByOwnerAccountIdOrderByStartDateAsc(me.getId());
        List<Occurrence> out = new ArrayList<>();
        for (CalendarEntry e : list) expand(e, from, to, true, out);
        for (Long professorId : professorIdsFor(me)) addDeadlines(professorId, from, to, out);
        sort(out);
        return new MyCalendar(list.stream().map(CalendarService::view).toList(), out, 0);
    }

    @Transactional
    public MyCalendar create(EntryInput input) {
        UserAccount me = requireOwner();
        if (entries.countByOwnerAccountId(me.getId()) >= MAX_ENTRIES) {
            throw bad("Your calendar is full. Delete some old entries first.");
        }
        CalendarEntry e = new CalendarEntry();
        e.setOwnerAccountId(me.getId());
        apply(e, input);
        int notified = 0;
        if (input.notifyStudents() && "PUBLIC".equals(e.getVisibility())) {
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            if (entries.countByOwnerAccountIdAndNotifiedTrueAndCreatedAtAfter(me.getId(), since) >= MAX_NOTIFY_PER_DAY) {
                throw bad("You can notify students up to " + MAX_NOTIFY_PER_DAY + " times a day. Save it without notifying.");
            }
            e.setNotified(true);
            entries.save(e);
            notified = notifyStudents(me, e);
        } else {
            entries.save(e);
        }
        return withRange(me, e.getStartDate(), notified);
    }

    @Transactional
    public MyCalendar update(Long id, EntryInput input) {
        UserAccount me = requireOwner();
        CalendarEntry e = entries.findByIdAndOwnerAccountId(id, me.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        apply(e, input);
        entries.save(e);
        return withRange(me, e.getStartDate(), 0);
    }

    @Transactional
    public void delete(Long id) {
        UserAccount me = requireOwner();
        CalendarEntry e = entries.findByIdAndOwnerAccountId(id, me.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        entries.delete(e);
    }

    // ---- What other people see (public entries only) ----

    @Transactional(readOnly = true)
    public Upcoming upcomingForAccount(Long accountId) {
        UserAccount owner = accounts.findById(accountId)
            .filter(a -> a.isActive() && OWNER_ROLES.contains(upper(a.getRole())))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar not found"));
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(UPCOMING_DAYS);
        List<Occurrence> out = new ArrayList<>();
        publicEntries(owner.getId(), from, to, out);
        if ("PROFESSOR".equals(upper(owner.getRole()))) {
            professors.findByUserAccountId(owner.getId()).ifPresent(p -> addDeadlines(p.getId(), from, to, out));
        }
        sort(out);
        return new Upcoming(displayName(owner), out.stream().limit(UPCOMING_LIMIT).toList());
    }

    @Transactional(readOnly = true)
    public Upcoming upcomingForProfessor(Long professorId) {
        Professor professor = professors.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar not found"));
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(UPCOMING_DAYS);
        List<Occurrence> out = new ArrayList<>();
        Long accountId = professor.getUserAccountId();
        if (accountId != null && accounts.findById(accountId).map(UserAccount::isActive).orElse(false)) {
            publicEntries(accountId, from, to, out);
        }
        addDeadlines(professor.getId(), from, to, out);
        sort(out);
        return new Upcoming(professor.getName(), out.stream().limit(UPCOMING_LIMIT).toList());
    }

    // ---- Helpers ----

    private MyCalendar withRange(UserAccount me, LocalDate around, int notified) {
        LocalDate from = around.withDayOfMonth(1).minusDays(7);
        MyCalendar view = mine(from, from.plusDays(49));
        return new MyCalendar(view.entries(), view.occurrences(), notified);
    }

    private void publicEntries(Long ownerId, LocalDate from, LocalDate to, List<Occurrence> out) {
        for (CalendarEntry e : entries.findByOwnerAccountIdOrderByStartDateAsc(ownerId)) {
            if ("PUBLIC".equals(e.getVisibility())) expand(e, from, to, false, out);
        }
    }

    private static void expand(CalendarEntry e, LocalDate from, LocalDate to, boolean mine, List<Occurrence> out) {
        long span = e.getEndDate() == null ? 0 : ChronoUnit.DAYS.between(e.getStartDate(), e.getEndDate());
        if (!e.isRepeatWeekly()) {
            LocalDate last = e.getStartDate().plusDays(span);
            if (!last.isBefore(from) && !e.getStartDate().isAfter(to)) out.add(occurrence(e, e.getStartDate(), span, mine));
            return;
        }
        LocalDate d = e.getStartDate();
        if (d.isBefore(from)) {
            long weeks = ChronoUnit.WEEKS.between(d, from.minusDays(span));
            if (weeks > 0) d = d.plusWeeks(weeks);
        }
        int guard = 0;
        while (!d.isAfter(to) && (e.getRepeatUntil() == null || !d.isAfter(e.getRepeatUntil())) && guard++ < 60) {
            if (!d.plusDays(span).isBefore(from)) out.add(occurrence(e, d, span, mine));
            d = d.plusWeeks(1);
        }
    }

    private static Occurrence occurrence(CalendarEntry e, LocalDate day, long span, boolean mine) {
        return new Occurrence(e.getId(), "ENTRY", e.getTitle(), day, span == 0 ? null : day.plusDays(span),
            e.getStartTime(), e.getEndTime(), e.getLocation(), e.getNotes(),
            e.getColor() == null ? "blue" : e.getColor(), mine ? e.getVisibility() : "PUBLIC",
            e.isRepeatWeekly(), null, Boolean.TRUE.equals(e.getAway()));
    }

    private void addDeadlines(Long professorId, LocalDate from, LocalDate to, List<Occurrence> out) {
        for (ResearchOpportunity o : opportunities.findByProfessorId(professorId)) {
            LocalDate d = o.getApplicationDeadline();
            if (d == null || d.isBefore(from) || d.isAfter(to)) continue;
            out.add(new Occurrence(null, "DEADLINE", "Applications close: " + o.getTitle(), d, null, null, null,
                null, null, "coral", "PUBLIC", false, o.getId(), false));
        }
    }

    private static void sort(List<Occurrence> out) {
        out.sort(Comparator.comparing(Occurrence::date)
            .thenComparing(o -> o.away() ? 0 : 1)
            .thenComparing(o -> o.startTime() == null ? LocalTime.MIN : o.startTime()));
    }

    // Professors whose deadlines belong on this person's calendar: their own
    // profile, or the professors an associate works with.
    private List<Long> professorIdsFor(UserAccount me) {
        String role = upper(me.getRole());
        if ("PROFESSOR".equals(role)) {
            return professors.findByUserAccountId(me.getId()).map(p -> List.of(p.getId())).orElse(List.of());
        }
        if ("ASSOCIATE".equals(role)) {
            return links.findByAssociateUserAccountIdOrderByIdAsc(me.getId()).stream()
                .map(AssociateLink::getProfessorId).distinct().toList();
        }
        return List.of();
    }

    // Bell alert (no email) to students who applied to or work on this person's projects.
    private int notifyStudents(UserAccount me, CalendarEntry e) {
        Set<Long> recipients = new LinkedHashSet<>();
        for (Long professorId : professorIdsFor(me)) {
            for (ResearchOpportunity o : opportunities.findByProfessorId(professorId)) {
                for (Application a : applications.findByResearchOpportunityId(o.getId())) {
                    if (a.getOpportunityStatus() == Application.OpportunityStatus.BOOKMARKED) continue;
                    if (a.getApplicationStatus() == Application.ApplicationStatus.REJECTED) continue;
                    if (a.getStudent() != null && a.getStudent().getUserAccountId() != null) {
                        recipients.add(a.getStudent().getUserAccountId());
                    }
                }
            }
        }
        recipients.remove(me.getId());
        String when = e.getStartDate().format(DateTimeFormatter.ofPattern("EEE MMM d", Locale.US))
            + (e.getStartTime() == null ? "" : " at " + e.getStartTime().format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)));
        String message = displayName(me) + " posted: " + e.getTitle() + " (" + when + ")";
        int sent = 0;
        for (Long recipient : recipients) {
            if (sent >= MAX_RECIPIENTS) break;
            try {
                Notification n = new Notification();
                n.setRecipientUserAccountId(recipient);
                n.setType("CALENDAR");
                n.setMessage(message.length() > 500 ? message.substring(0, 500) : message);
                n.setLink("/discover-opportunities");
                notifications.save(n);
                sent++;
            } catch (Exception ex) {
                logger.warn("[CALENDAR] Could not record notification: {}", ex.getMessage());
            }
        }
        return sent;
    }

    private void apply(CalendarEntry e, EntryInput in) {
        if (in == null) throw bad("Fill in the entry");
        String title = clean(in.title(), 150, "Title");
        if (title == null) throw bad("Add a title");
        if (in.startDate() == null) throw bad("Choose a date");
        LocalDate today = LocalDate.now();
        if (in.startDate().isBefore(today.minusYears(1)) || in.startDate().isAfter(today.plusYears(2))) {
            throw bad("Choose a date within the next two years");
        }
        boolean away = in.away();
        LocalDate end = in.endDate();
        if (end != null && end.equals(in.startDate())) end = null;
        if (end != null && end.isBefore(in.startDate())) throw bad("The end date is before the start date");
        if (end != null && ChronoUnit.DAYS.between(in.startDate(), end) > 60) throw bad("An entry can span up to 60 days");
        LocalTime startTime = away ? null : in.startTime();
        LocalTime endTime = away ? null : in.endTime();
        boolean repeat = !away && in.repeatWeekly();
        if (endTime != null && startTime == null) throw bad("Add a start time");
        if (startTime != null && endTime != null && end == null && !endTime.isAfter(startTime)) {
            throw bad("The end time is before the start time");
        }
        if (repeat && end != null && ChronoUnit.DAYS.between(in.startDate(), end) >= 7) {
            throw bad("A weekly entry can't be longer than a week");
        }
        LocalDate until = repeat ? in.repeatUntil() : null;
        if (until != null && until.isBefore(in.startDate())) throw bad("Repeat until is before the start date");
        if (until != null && until.isAfter(in.startDate().plusYears(1))) throw bad("Repeat for up to a year");
        String color = away ? "gray"
            : in.color() == null || in.color().isBlank() ? "blue" : in.color().trim().toLowerCase(Locale.ROOT);
        if (!COLORS.contains(color)) throw bad("Pick one of the listed colors");
        String visibility = upper(in.visibility());
        if (!"PUBLIC".equals(visibility) && !"PRIVATE".equals(visibility)) throw bad("Choose who can see this");

        e.setTitle(title);
        e.setStartDate(in.startDate());
        e.setEndDate(end);
        e.setStartTime(startTime);
        e.setEndTime(endTime);
        e.setRepeatWeekly(repeat);
        e.setAway(away);
        e.setRepeatUntil(until);
        e.setLocation(clean(in.location(), 300, "Place or link"));
        e.setNotes(clean(in.notes(), 2000, "Notes"));
        e.setColor(color);
        e.setVisibility(visibility);
    }

    private UserAccount requireOwner() {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (!OWNER_ROLES.contains(upper(me.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Calendars are for faculty and Research Associates");
        }
        return me;
    }

    private String displayName(UserAccount a) {
        String role = upper(a.getRole());
        if ("PROFESSOR".equals(role)) {
            String name = professors.findByUserAccountId(a.getId()).map(Professor::getName).orElse(null);
            if (name != null && !name.isBlank()) return name;
        }
        if ("ASSOCIATE".equals(role)) {
            String name = associateProfiles.findByUserAccountId(a.getId()).map(AssociateProfile::getName).orElse(null);
            if (name != null && !name.isBlank()) return name;
        }
        return a.getUsername();
    }

    private static EntryView view(CalendarEntry e) {
        return new EntryView(e.getId(), e.getTitle(), e.getStartDate(), e.getEndDate(), e.getStartTime(),
            e.getEndTime(), e.isRepeatWeekly(), e.getRepeatUntil(), e.getLocation(), e.getNotes(),
            e.getColor(), e.getVisibility(), e.isNotified(), Boolean.TRUE.equals(e.getAway()));
    }

    private static String upper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    private static String clean(String value, int max, String label) {
        if (value == null) return null;
        String v = value.trim();
        if (v.length() > max) throw bad(label + " is too long (up to " + max + " characters)");
        return v.isEmpty() ? null : v;
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
