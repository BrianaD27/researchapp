package com.vsu.researchapp.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.AssociateProfile;
import com.vsu.researchapp.domain.model.DirectConversation;
import com.vsu.researchapp.domain.model.DirectMessage;
import com.vsu.researchapp.domain.model.MessagingPrefs;
import com.vsu.researchapp.domain.model.Notification;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateProfileRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.DirectConversationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.DirectMessageRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.MessagingPrefsRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.NotificationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.StudentJpaRepository;
import com.vsu.researchapp.infrastructure.externalServices.email.emailService;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// 1:1 messages between any two people in the VSU app: students, faculty,
// Research Associates, and admins. Partner-school and not-yet-approved faculty
// accounts can't message (SecurityConfig also blocks them from /api/messages).
// - Only the two people in a conversation can read it. Admins have no access to
//   other people's conversations; a reported message is copied into the report.
// - Anyone can report a message they received; it goes to the admins as a
//   problem report with the message and a little context.
// - Limits: 60 messages per 10 minutes and 30 new conversations per day per person.
// - Each new message raises the bell right away. Email is at most once per day per
//   person, generic ("You have a new message waiting"), plus an 8 AM reminder.
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    public static final int MAX_LENGTH = 2000;
    private static final Set<String> MESSAGING_ROLES = Set.of("STUDENT", "PROFESSOR", "ASSOCIATE", "ADMIN");
    private static final int MAX_MESSAGES_PER_10_MIN = 60;
    private static final int MAX_NEW_CONVERSATIONS_PER_DAY = 30;
    private static final int SEARCH_LIMIT = 20;
    public static final String REPORT_CATEGORY = "Inappropriate message";

    public record ConversationDto(Long id, Long otherAccountId, String otherName, String otherDetail,
                                  boolean otherActive, String lastMessagePreview, LocalDateTime lastMessageAt,
                                  long unread) {}

    public record MessageDto(Long id, String senderName, boolean mine, String body, LocalDateTime createdAt) {}

    public record ThreadDto(ConversationDto conversation, List<MessageDto> messages) {}

    public record PersonDto(Long accountId, String name, String detail, boolean active, Long conversationId) {}

    private record Who(String name, String detail) {}

    private final DirectConversationRepository conversations;
    private final DirectMessageRepository messages;
    private final MessagingPrefsRepository prefs;
    private final NotificationRepository notifications;
    private final UserAccountRepository accounts;
    private final ProfessorJpaRepository professors;
    private final StudentJpaRepository students;
    private final AssociateProfileRepository associateProfiles;
    private final CurrentUserService currentUser;
    private final PresenceService presence;
    private final ProblemReportService problemReports;
    private final emailService email;

    public MessageService(DirectConversationRepository conversations, DirectMessageRepository messages,
            MessagingPrefsRepository prefs, NotificationRepository notifications, UserAccountRepository accounts,
            ProfessorJpaRepository professors, StudentJpaRepository students,
            AssociateProfileRepository associateProfiles, CurrentUserService currentUser, PresenceService presence,
            ProblemReportService problemReports, emailService email) {
        this.conversations = conversations;
        this.messages = messages;
        this.prefs = prefs;
        this.notifications = notifications;
        this.accounts = accounts;
        this.professors = professors;
        this.students = students;
        this.associateProfiles = associateProfiles;
        this.currentUser = currentUser;
        this.presence = presence;
        this.problemReports = problemReports;
        this.email = email;
    }

    // ---- reading ----

    @Transactional(readOnly = true)
    public List<ConversationDto> conversations() {
        UserAccount me = me();
        return conversations.findByUserLowIdOrUserHighId(me.getId(), me.getId()).stream()
            .sorted(Comparator.comparing(DirectConversation::getLastMessageAt,
                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
            .map(c -> toDto(me, c))
            .toList();
    }

    @Transactional
    public ThreadDto thread(Long conversationId) {
        UserAccount me = me();
        DirectConversation c = mine(me, conversationId);
        markRead(me, c, LocalDateTime.now());
        conversations.save(c);
        List<DirectMessage> latest = new ArrayList<>(messages.findTop200ByConversationIdOrderByCreatedAtDesc(c.getId()));
        Collections.reverse(latest);
        String myName = who(me).name();
        String otherName = who(other(me, c)).name();
        List<MessageDto> items = latest.stream()
            .map(m -> {
                boolean mine = m.getSenderAccountId().equals(me.getId());
                return new MessageDto(m.getId(), mine ? myName : otherName, mine, m.getBody(), m.getCreatedAt());
            })
            .toList();
        return new ThreadDto(toDto(me, c), items);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (!canMessage(me)) return 0;
        return conversations.findByUserLowIdOrUserHighId(me.getId(), me.getId()).stream()
            .mapToLong(c -> unread(me.getId(), c)).sum();
    }

    // Find anyone in the VSU app by name or username.
    @Transactional(readOnly = true)
    public List<PersonDto> people(String query) {
        UserAccount me = me();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.length() < 2) return List.of();
        if (q.length() > 100) q = q.substring(0, 100);
        Map<Long, Who> names = directory();
        List<PersonDto> out = new ArrayList<>();
        for (UserAccount a : accounts.findAll()) {
            if (a.getId().equals(me.getId()) || !canMessage(a)) continue;
            Who w = names.getOrDefault(a.getId(), fallback(a));
            String name = w.name().toLowerCase(Locale.ROOT);
            String username = a.getUsername() == null ? "" : a.getUsername().toLowerCase(Locale.ROOT);
            if (!name.contains(q) && !username.contains(q)) continue;
            out.add(new PersonDto(a.getId(), w.name(), w.detail(), presence.isActive(a.getId()),
                existing(me.getId(), a.getId()).map(DirectConversation::getId).orElse(null)));
        }
        out.sort(Comparator.comparing((PersonDto p) -> p.name().toLowerCase(Locale.ROOT)));
        return out.stream().limit(SEARCH_LIMIT).toList();
    }

    // ---- active-now dots outside the Messages page ----
    // Everyone signed in to the VSU app can see who is online (people who turned
    // off "Show when I'm active" always show as offline).

    @Transactional(readOnly = true)
    public Map<Long, Boolean> studentPresence(List<Long> studentIds) {
        Map<Long, Boolean> out = new LinkedHashMap<>();
        if (studentIds == null) return out;
        for (Student s : students.findAllById(studentIds.stream().limit(200).toList())) {
            out.put(s.getId(), presence.isActive(s.getUserAccountId()));
        }
        return out;
    }

    public Map<Long, Boolean> accountPresence(List<Long> accountIds) {
        Map<Long, Boolean> out = new LinkedHashMap<>();
        if (accountIds == null) return out;
        for (Long id : accountIds.stream().limit(500).toList()) {
            out.put(id, presence.isActive(id));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> professorPresence(List<Long> professorIds) {
        Map<Long, Boolean> out = new LinkedHashMap<>();
        if (professorIds == null) return out;
        for (Professor p : professors.findAllById(professorIds.stream().limit(200).toList())) {
            out.put(p.getId(), presence.isActive(p.getUserAccountId()));
        }
        return out;
    }

    // ---- writing ----

    // kind = "account" (login account id), "student" (student profile id), or
    // "professor" (professor profile id).
    @Transactional
    public ConversationDto start(String kind, Long id) {
        UserAccount me = me();
        if (id == null) throw bad("Pick who to message");
        Long targetAccountId = switch (kind == null ? "account" : kind.toLowerCase(Locale.ROOT)) {
            case "student" -> students.findById(id).map(Student::getUserAccountId)
                .orElseThrow(() -> notFound("Student not found"));
            case "professor" -> professors.findById(id).map(Professor::getUserAccountId)
                .orElseThrow(() -> notFound("Professor not found"));
            case "account" -> id;
            default -> throw bad("Pick who to message");
        };
        if (targetAccountId == null) throw bad("This person's profile isn't linked to an account yet");
        if (targetAccountId.equals(me.getId())) throw bad("You can't message yourself");
        UserAccount target = accounts.findById(targetAccountId).filter(this::canMessage)
            .orElseThrow(() -> notFound("This person can't receive messages"));

        DirectConversation c = existing(me.getId(), target.getId()).orElse(null);
        if (c == null) {
            if (conversations.countByCreatedByAndCreatedAtAfter(me.getId(), LocalDateTime.now().minusDays(1))
                    >= MAX_NEW_CONVERSATIONS_PER_DAY) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "You've started a lot of new conversations today. Try again tomorrow.");
            }
            c = new DirectConversation();
            c.setUserLowId(Math.min(me.getId(), target.getId()));
            c.setUserHighId(Math.max(me.getId(), target.getId()));
            c.setCreatedBy(me.getId());
            c = conversations.save(c);
        }
        return toDto(me, c);
    }

    @Transactional
    public MessageDto send(Long conversationId, String body) {
        UserAccount me = me();
        DirectConversation c = mine(me, conversationId);
        String text = body == null ? "" : body.strip();
        if (text.isEmpty()) throw bad("Type a message first");
        if (text.length() > MAX_LENGTH) throw bad("Messages can be up to 2,000 characters");
        if (messages.countBySenderAccountIdAndCreatedAtAfter(me.getId(), LocalDateTime.now().minusMinutes(10))
                >= MAX_MESSAGES_PER_10_MIN) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "You're sending messages very fast. Wait a few minutes and try again.");
        }
        UserAccount other = other(me, c);
        if (!canMessage(other)) throw bad("This person can't receive messages right now");

        DirectMessage m = new DirectMessage();
        m.setConversationId(c.getId());
        m.setSenderAccountId(me.getId());
        m.setBody(text);
        m = messages.save(m);

        c.setLastMessageAt(m.getCreatedAt());
        c.setLastMessagePreview(clip(text.replaceAll("\\s+", " "), 200));
        markRead(me, c, m.getCreatedAt());
        conversations.save(c);

        String myName = who(me).name();
        alert(other.getId(), myName, c.getId());
        return new MessageDto(m.getId(), myName, true, m.getBody(), m.getCreatedAt());
    }

    // Report a message you received. The admins get a problem report with the
    // message, who sent it, and the messages just before it.
    @Transactional
    public void report(Long messageId, String reason) {
        UserAccount me = me();
        DirectMessage m = messages.findById(messageId).orElseThrow(() -> notFound("Message not found"));
        DirectConversation c = mine(me, m.getConversationId());
        if (m.getSenderAccountId().equals(me.getId())) throw bad("You can't report your own message");
        UserAccount sender = accounts.findById(m.getSenderAccountId()).orElse(null);
        String senderLabel = sender == null ? "unknown account"
            : who(sender).name() + " (username " + sender.getUsername() + ", account #" + sender.getId() + ")";
        DateTimeFormatter when = DateTimeFormatter.ofPattern("MMM d h:mm a", Locale.US);

        StringBuilder text = new StringBuilder();
        text.append("Reported message from ").append(senderLabel).append(":\n\"")
            .append(clip(m.getBody(), 800)).append("\"\n");
        String why = reason == null ? "" : reason.trim();
        text.append("Reason: ").append(why.isEmpty() ? "(none given)" : clip(why, 400)).append("\n");
        text.append("Conversation #").append(c.getId()).append(", recent messages:\n");
        List<DirectMessage> context = new ArrayList<>(
            messages.findTop6ByConversationIdAndIdLessThanEqualOrderByIdDesc(c.getId(), m.getId()));
        Collections.reverse(context);
        for (DirectMessage x : context) {
            String from = x.getSenderAccountId().equals(me.getId()) ? "Reporter" : "Reported person";
            String at = x.getCreatedAt() == null ? "" : x.getCreatedAt().format(when);
            text.append("- ").append(from).append(" (").append(at).append("): ")
                .append(clip(x.getBody().replaceAll("\\s+", " "), 150)).append("\n");
        }
        problemReports.submit(REPORT_CATEGORY, clip(text.toString(), 2000), "/messages", null);
    }

    // ---- daily reminder ----

    @Scheduled(cron = "0 0 8 * * *", zone = "America/New_York")
    @Transactional
    public void dailyReminder() {
        Set<Long> waiting = new HashSet<>();
        for (DirectConversation c : conversations.findByLastMessageAtAfter(LocalDateTime.now().minusDays(14))) {
            if (unread(c.getUserLowId(), c) > 0) waiting.add(c.getUserLowId());
            if (unread(c.getUserHighId(), c) > 0) waiting.add(c.getUserHighId());
        }
        waiting.forEach(this::maybeEmail);
        logger.info("[MESSAGES] Daily reminder checked {} people with unread messages", waiting.size());
    }

    // ---- helpers ----

    private UserAccount me() {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (!canMessage(me)) throw forbidden("Messages aren't available for this account");
        return me;
    }

    private boolean canMessage(UserAccount a) {
        return a != null && a.isActive()
            && MESSAGING_ROLES.contains(a.getRole() == null ? "" : a.getRole().toUpperCase(Locale.ROOT))
            && !Boolean.FALSE.equals(a.getEmailVerified());
    }

    private java.util.Optional<DirectConversation> existing(Long a, Long b) {
        return conversations.findByUserLowIdAndUserHighId(Math.min(a, b), Math.max(a, b));
    }

    // Not yours -> "not found", so ids of other people's conversations reveal nothing.
    private DirectConversation mine(UserAccount me, Long conversationId) {
        DirectConversation c = conversations.findById(conversationId)
            .orElseThrow(() -> notFound("Conversation not found"));
        if (!me.getId().equals(c.getUserLowId()) && !me.getId().equals(c.getUserHighId())) {
            throw notFound("Conversation not found");
        }
        return c;
    }

    private UserAccount other(UserAccount me, DirectConversation c) {
        Long otherId = me.getId().equals(c.getUserLowId()) ? c.getUserHighId() : c.getUserLowId();
        return accounts.findById(otherId).orElseThrow(() -> notFound("Conversation not found"));
    }

    private static void markRead(UserAccount me, DirectConversation c, LocalDateTime at) {
        if (me.getId().equals(c.getUserLowId())) c.setLowLastReadAt(at);
        else c.setHighLastReadAt(at);
    }

    private long unread(Long accountId, DirectConversation c) {
        LocalDateTime read = accountId.equals(c.getUserLowId()) ? c.getLowLastReadAt() : c.getHighLastReadAt();
        return read == null
            ? messages.countByConversationIdAndSenderAccountIdNot(c.getId(), accountId)
            : messages.countByConversationIdAndSenderAccountIdNotAndCreatedAtAfter(c.getId(), accountId, read);
    }

    private ConversationDto toDto(UserAccount me, DirectConversation c) {
        Long otherId = me.getId().equals(c.getUserLowId()) ? c.getUserHighId() : c.getUserLowId();
        UserAccount other = accounts.findById(otherId).orElse(null);
        Who w = other == null ? new Who("Deleted account", null) : who(other);
        return new ConversationDto(c.getId(), otherId, w.name(), w.detail(), presence.isActive(otherId),
            c.getLastMessagePreview(), c.getLastMessageAt(), unread(me.getId(), c));
    }

    // Display name and a short description for one account.
    private Who who(UserAccount a) {
        String role = a.getRole() == null ? "" : a.getRole().toUpperCase(Locale.ROOT);
        switch (role) {
            case "STUDENT" -> {
                Student s = students.findByUserAccountId(a.getId()).orElse(null);
                if (s != null) return new Who(nameOr(s.getName(), a), join("Student", s.getMajor()));
            }
            case "PROFESSOR" -> {
                Professor p = professors.findByUserAccountId(a.getId()).orElse(null);
                if (p != null) return new Who(nameOr(p.getName(), a), join("Faculty", p.getDepartment()));
            }
            case "ASSOCIATE" -> {
                AssociateProfile ap = associateProfiles.findByUserAccountId(a.getId()).orElse(null);
                if (ap != null) return new Who(nameOr(ap.getName(), a), join("Research Associate", ap.getDepartment()));
            }
            default -> { }
        }
        return fallback(a);
    }

    // Everyone's display names at once, for search.
    private Map<Long, Who> directory() {
        Map<Long, Who> out = new HashMap<>();
        for (Student s : students.findAll()) {
            if (s.getUserAccountId() != null && s.getName() != null && !s.getName().isBlank()) {
                out.put(s.getUserAccountId(), new Who(s.getName(), join("Student", s.getMajor())));
            }
        }
        for (Professor p : professors.findAll()) {
            if (p.getUserAccountId() != null && p.getName() != null && !p.getName().isBlank()) {
                out.put(p.getUserAccountId(), new Who(p.getName(), join("Faculty", p.getDepartment())));
            }
        }
        for (AssociateProfile ap : associateProfiles.findAll()) {
            if (ap.getName() != null && !ap.getName().isBlank()) {
                out.put(ap.getUserAccountId(), new Who(ap.getName(), join("Research Associate", ap.getDepartment())));
            }
        }
        return out;
    }

    private static Who fallback(UserAccount a) {
        String role = a.getRole() == null ? "" : a.getRole().toUpperCase(Locale.ROOT);
        String label = switch (role) {
            case "STUDENT" -> "Student";
            case "PROFESSOR" -> "Faculty";
            case "ASSOCIATE" -> "Research Associate";
            case "ADMIN" -> "Admin";
            default -> null;
        };
        return new Who(a.getUsername(), label);
    }

    private static String nameOr(String name, UserAccount a) {
        return name == null || name.isBlank() ? a.getUsername() : name;
    }

    private static String join(String label, String extra) {
        return extra == null || extra.isBlank() ? label : label + " - " + extra;
    }

    // Bell alert now (one unread alert per conversation, not one per message), email at most daily.
    private void alert(Long recipient, String fromName, Long conversationId) {
        try {
            String link = "/messages?c=" + conversationId;
            boolean pending = notifications.findTop30ByRecipientUserAccountIdOrderByCreatedAtDesc(recipient).stream()
                .anyMatch(n -> !n.isRead() && link.equals(n.getLink()));
            if (!pending) {
                Notification n = new Notification();
                n.setRecipientUserAccountId(recipient);
                n.setType("MESSAGE");
                n.setMessage(clip("New message from " + fromName, 500));
                n.setLink(link);
                notifications.save(n);
            }
            maybeEmail(recipient);
        } catch (Exception e) {
            logger.warn("[MESSAGES] Could not alert recipient: {}", e.getMessage());
        }
    }

    private void maybeEmail(Long userAccountId) {
        MessagingPrefs p = prefs.findById(userAccountId).orElseGet(() -> {
            MessagingPrefs created = new MessagingPrefs();
            created.setUserAccountId(userAccountId);
            return created;
        });
        LocalDateTime now = LocalDateTime.now();
        if (p.getLastMessageEmailAt() != null && p.getLastMessageEmailAt().isAfter(now.minusHours(24))) return;
        String address = accounts.findById(userAccountId).filter(UserAccount::isActive)
            .map(UserAccount::getEmail).orElse(null);
        if (address == null || address.isBlank()) return;
        p.setLastMessageEmailAt(now);
        prefs.save(p);
        CompletableFuture.runAsync(() -> {
            try {
                email.sendNotification(address, "You have a new message waiting",
                    "You have a new message waiting in the VSU Research App. Log in to read and reply.", "/messages");
            } catch (Exception e) {
                logger.warn("[EMAIL] Could not send message alert: {}", e.getMessage());
            }
        });
    }

    private static String clip(String value, int max) {
        return value == null ? "" : value.length() > max ? value.substring(0, max) : value;
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
