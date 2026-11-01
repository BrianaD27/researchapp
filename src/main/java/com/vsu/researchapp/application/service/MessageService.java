package com.vsu.researchapp.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.AssociateLink;
import com.vsu.researchapp.domain.model.Conversation;
import com.vsu.researchapp.domain.model.Message;
import com.vsu.researchapp.domain.model.MessagingPrefs;
import com.vsu.researchapp.domain.model.Notification;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ConversationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.MessageRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.MessagingPrefsRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.NotificationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ApplicationJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.StudentJpaRepository;
import com.vsu.researchapp.infrastructure.externalServices.email.emailService;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// 1:1 messages between a student and a professor.
// - A student can start a conversation only with a professor whose project they applied to.
// - A professor (or their Research Associate with review permission, acting for them)
//   can message any student who has a profile.
// - Only the two sides can read a thread. Admins have no access unless they are
//   themselves the professor in the thread.
// - Each new message raises the bell right away. Email is at most once per day per
//   person, generic ("You have a new message waiting"), plus an 8 AM reminder if
//   messages are still unread and nothing was emailed in the last 24 hours.
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    public static final int MAX_LENGTH = 2000;
    private static final String STUDENT = "STUDENT";
    private static final String FACULTY = "FACULTY";

    public record Ctx(UserAccount me, String side, Long professorId, String displayName) {}

    public record ConversationDto(Long id, String otherName, String otherDetail, boolean otherActive,
                                  String lastMessagePreview, LocalDateTime lastMessageAt, long unread) {}

    public record MessageDto(Long id, String senderName, boolean mine, String body, LocalDateTime createdAt) {}

    public record ThreadDto(ConversationDto conversation, List<MessageDto> messages) {}

    public record ContactDto(Long id, String name, String detail, boolean active, Long conversationId) {}

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final MessagingPrefsRepository prefs;
    private final NotificationRepository notifications;
    private final UserAccountRepository accounts;
    private final AssociateLinkRepository associateLinks;
    private final ProfessorJpaRepository professors;
    private final StudentJpaRepository students;
    private final ApplicationJpaRepository applications;
    private final CurrentUserService currentUser;
    private final PresenceService presence;
    private final emailService email;

    public MessageService(ConversationRepository conversations, MessageRepository messages,
            MessagingPrefsRepository prefs, NotificationRepository notifications, UserAccountRepository accounts,
            AssociateLinkRepository associateLinks, ProfessorJpaRepository professors, StudentJpaRepository students,
            ApplicationJpaRepository applications, CurrentUserService currentUser, PresenceService presence,
            emailService email) {
        this.conversations = conversations;
        this.messages = messages;
        this.prefs = prefs;
        this.notifications = notifications;
        this.accounts = accounts;
        this.associateLinks = associateLinks;
        this.professors = professors;
        this.students = students;
        this.applications = applications;
        this.currentUser = currentUser;
        this.presence = presence;
        this.email = email;
    }

    // ---- who am I in messaging? ----

    public Ctx context(String actingFor) {
        UserAccount me = currentUser.getCurrentUserAccount();
        String role = me.getRole() == null ? "" : me.getRole().toUpperCase();
        if (STUDENT.equals(role)) {
            String name = students.findByUserAccountId(me.getId()).map(Student::getName)
                .filter(n -> n != null && !n.isBlank()).orElse(me.getUsername());
            return new Ctx(me, STUDENT, null, name);
        }
        if ("ASSOCIATE".equals(role)) {
            AssociateLink link = associateLinks.findByAssociateUserAccountIdOrderByIdAsc(me.getId()).stream()
                .filter(AssociateLink::isCanReview)
                .filter(l -> actingFor == null || actingFor.isBlank() || String.valueOf(l.getProfessorId()).equals(actingFor))
                .findFirst()
                .orElseThrow(() -> forbidden("You need review permission from a professor to use messages"));
            String profName = professors.findById(link.getProfessorId()).map(Professor::getName).orElse("professor");
            return new Ctx(me, FACULTY, link.getProfessorId(), me.getUsername() + " (for " + profName + ")");
        }
        Professor professor = professors.findByUserAccountId(me.getId())
            .orElseThrow(() -> forbidden("Messages are for students and faculty with a profile"));
        String name = professor.getName() == null || professor.getName().isBlank() ? me.getUsername() : professor.getName();
        return new Ctx(me, FACULTY, professor.getId(), name);
    }

    // ---- reading ----

    @Transactional(readOnly = true)
    public List<ConversationDto> conversations(Ctx ctx) {
        List<Conversation> list = STUDENT.equals(ctx.side())
            ? conversations.findByStudentUserAccountIdOrderByLastMessageAtDesc(ctx.me().getId())
            : conversations.findByProfessorIdOrderByLastMessageAtDesc(ctx.professorId());
        return list.stream().map(c -> toDto(ctx, c)).toList();
    }

    @Transactional
    public ThreadDto thread(Ctx ctx, Long conversationId) {
        Conversation c = mine(ctx, conversationId);
        if (STUDENT.equals(ctx.side())) c.setStudentLastReadAt(LocalDateTime.now());
        else c.setFacultyLastReadAt(LocalDateTime.now());
        conversations.save(c);
        List<Message> latest = new ArrayList<>(messages.findTop200ByConversationIdOrderByCreatedAtDesc(c.getId()));
        Collections.reverse(latest);
        List<MessageDto> items = latest.stream()
            .map(m -> new MessageDto(m.getId(), m.getSenderName(), ctx.side().equals(m.getSenderSide()), m.getBody(), m.getCreatedAt()))
            .toList();
        return new ThreadDto(toDto(ctx, c), items);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Ctx ctx) {
        return conversations(ctx).stream().mapToLong(ConversationDto::unread).sum();
    }

    // People you are allowed to start a conversation with.
    @Transactional(readOnly = true)
    public List<ContactDto> contacts(Ctx ctx) {
        Map<Long, ContactDto> out = new LinkedHashMap<>();
        if (STUDENT.equals(ctx.side())) {
            Student me = students.findByUserAccountId(ctx.me().getId()).orElse(null);
            if (me == null) return List.of();
            for (Application a : applications.findByStudentId(me.getId())) {
                ResearchOpportunity o = a.getResearchOpportunity();
                Professor p = o == null ? null : o.getProfessor();
                if (p == null || p.getUserAccountId() == null || out.containsKey(p.getId())) continue;
                Long convId = conversations.findByStudentUserAccountIdAndProfessorId(ctx.me().getId(), p.getId())
                    .map(Conversation::getId).orElse(null);
                out.put(p.getId(), new ContactDto(p.getId(), p.getName(), p.getDepartment(),
                    presence.isActive(p.getUserAccountId()), convId));
            }
        } else {
            Professor p = professors.findById(ctx.professorId()).orElse(null);
            if (p == null || p.getResearchOpportunities() == null) return List.of();
            for (ResearchOpportunity o : p.getResearchOpportunities()) {
                for (Application a : applications.findByResearchOpportunityId(o.getId())) {
                    Student s = a.getStudent();
                    if (s == null || s.getUserAccountId() == null || out.containsKey(s.getId())) continue;
                    Long convId = conversations.findByStudentUserAccountIdAndProfessorId(s.getUserAccountId(), p.getId())
                        .map(Conversation::getId).orElse(null);
                    out.put(s.getId(), new ContactDto(s.getId(), s.getName(), "Applied to " + o.getTitle(),
                        presence.isActive(s.getUserAccountId()), convId));
                }
            }
        }
        return new ArrayList<>(out.values());
    }

    // ---- active-now dots outside the Messages page ----
    // Students see faculty; faculty and associates see students; students never
    // see other students. Anyone else just gets an empty answer.

    @Transactional(readOnly = true)
    public Map<Long, Boolean> studentPresence(Ctx ctx, List<Long> studentIds) {
        Map<Long, Boolean> out = new LinkedHashMap<>();
        if (!FACULTY.equals(ctx.side()) || studentIds == null) return out;
        for (Student s : students.findAllById(studentIds.stream().limit(200).toList())) {
            out.put(s.getId(), presence.isActive(s.getUserAccountId()));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> professorPresence(Ctx ctx, List<Long> professorIds) {
        Map<Long, Boolean> out = new LinkedHashMap<>();
        if (!STUDENT.equals(ctx.side()) || professorIds == null) return out;
        for (Professor p : professors.findAllById(professorIds.stream().limit(200).toList())) {
            out.put(p.getId(), presence.isActive(p.getUserAccountId()));
        }
        return out;
    }

    // ---- writing ----

    // Students pass a professorId; faculty pass a student profile id.
    @Transactional
    public ConversationDto start(Ctx ctx, Long targetId) {
        if (targetId == null) throw bad("Pick who to message");
        Conversation c;
        if (STUDENT.equals(ctx.side())) {
            Student me = students.findByUserAccountId(ctx.me().getId())
                .orElseThrow(() -> bad("Create your student profile before messaging"));
            Professor p = professors.findById(targetId).orElseThrow(() -> notFound("Professor not found"));
            boolean applied = applications.findByStudentId(me.getId()).stream()
                .anyMatch(a -> a.getResearchOpportunity() != null && a.getResearchOpportunity().getProfessor() != null
                    && p.getId().equals(a.getResearchOpportunity().getProfessor().getId()));
            if (!applied) throw forbidden("You can message a professor after applying to one of their projects");
            if (p.getUserAccountId() == null) throw bad("This professor's profile isn't linked to an account yet");
            c = conversations.findByStudentUserAccountIdAndProfessorId(ctx.me().getId(), p.getId())
                .orElseGet(() -> create(ctx.me().getId(), p));
        } else {
            Student s = students.findById(targetId).orElseThrow(() -> notFound("Student not found"));
            if (s.getUserAccountId() == null) throw bad("This student's profile isn't linked to an account yet");
            Professor p = professors.findById(ctx.professorId()).orElseThrow(() -> notFound("Professor not found"));
            if (p.getUserAccountId() == null) throw bad("The professor's profile isn't linked to an account yet");
            c = conversations.findByStudentUserAccountIdAndProfessorId(s.getUserAccountId(), p.getId())
                .orElseGet(() -> create(s.getUserAccountId(), p));
        }
        return toDto(ctx, c);
    }

    @Transactional
    public MessageDto send(Ctx ctx, Long conversationId, String body) {
        Conversation c = mine(ctx, conversationId);
        String text = body == null ? "" : body.strip();
        if (text.isEmpty()) throw bad("Type a message first");
        if (text.length() > MAX_LENGTH) throw bad("Messages can be up to 2,000 characters");

        Message m = new Message();
        m.setConversationId(c.getId());
        m.setSenderUserAccountId(ctx.me().getId());
        m.setSenderName(clip(ctx.displayName(), 150));
        m.setSenderSide(ctx.side());
        m.setBody(text);
        m = messages.save(m);

        c.setLastMessageAt(m.getCreatedAt());
        c.setLastMessagePreview(clip(text.replaceAll("\\s+", " "), 200));
        if (STUDENT.equals(ctx.side())) c.setStudentLastReadAt(m.getCreatedAt());
        else c.setFacultyLastReadAt(m.getCreatedAt());
        conversations.save(c);

        for (Long recipient : recipients(c, ctx.side())) {
            if (!recipient.equals(ctx.me().getId())) alert(recipient, ctx.displayName(), c.getId());
        }
        return new MessageDto(m.getId(), m.getSenderName(), true, m.getBody(), m.getCreatedAt());
    }

    // ---- daily reminder ----

    @Scheduled(cron = "0 0 8 * * *", zone = "America/New_York")
    @Transactional
    public void dailyReminder() {
        Set<Long> waiting = new HashSet<>();
        for (Conversation c : conversations.findByLastMessageAtAfter(LocalDateTime.now().minusDays(14))) {
            if (unread(c, STUDENT) > 0) waiting.add(c.getStudentUserAccountId());
            if (unread(c, FACULTY) > 0) waiting.add(c.getProfessorUserAccountId());
        }
        waiting.forEach(this::maybeEmail);
        logger.info("[MESSAGES] Daily reminder checked {} people with unread messages", waiting.size());
    }

    // ---- helpers ----

    private Conversation create(Long studentUserAccountId, Professor p) {
        Conversation c = new Conversation();
        c.setStudentUserAccountId(studentUserAccountId);
        c.setProfessorId(p.getId());
        c.setProfessorUserAccountId(p.getUserAccountId());
        return conversations.save(c);
    }

    // Not yours -> "not found", so ids of other people's threads reveal nothing.
    private Conversation mine(Ctx ctx, Long conversationId) {
        Conversation c = conversations.findById(conversationId).orElseThrow(() -> notFound("Conversation not found"));
        boolean member = STUDENT.equals(ctx.side())
            ? c.getStudentUserAccountId().equals(ctx.me().getId())
            : c.getProfessorId().equals(ctx.professorId());
        if (!member) throw notFound("Conversation not found");
        return c;
    }

    private long unread(Conversation c, String side) {
        LocalDateTime read = STUDENT.equals(side) ? c.getStudentLastReadAt() : c.getFacultyLastReadAt();
        return read == null
            ? messages.countByConversationIdAndSenderSideNot(c.getId(), side)
            : messages.countByConversationIdAndSenderSideNotAndCreatedAtAfter(c.getId(), side, read);
    }

    private ConversationDto toDto(Ctx ctx, Conversation c) {
        String name;
        String detail;
        boolean active;
        if (STUDENT.equals(ctx.side())) {
            Professor p = professors.findById(c.getProfessorId()).orElse(null);
            name = p == null ? "Professor" : p.getName();
            detail = p == null ? null : p.getDepartment();
            active = presence.isActive(c.getProfessorUserAccountId());
        } else {
            Student s = students.findByUserAccountId(c.getStudentUserAccountId()).orElse(null);
            name = s != null && s.getName() != null ? s.getName()
                : accounts.findById(c.getStudentUserAccountId()).map(UserAccount::getUsername).orElse("Student");
            detail = s == null ? null : s.getMajor();
            active = presence.isActive(c.getStudentUserAccountId());
        }
        return new ConversationDto(c.getId(), name, detail, active, c.getLastMessagePreview(), c.getLastMessageAt(),
            unread(c, ctx.side()));
    }

    private List<Long> recipients(Conversation c, String senderSide) {
        if (FACULTY.equals(senderSide)) return List.of(c.getStudentUserAccountId());
        List<Long> out = new ArrayList<>();
        out.add(c.getProfessorUserAccountId());
        for (AssociateLink link : associateLinks.findByProfessorIdOrderByIdAsc(c.getProfessorId())) {
            if (link.isCanReview()) out.add(link.getAssociateUserAccountId());
        }
        return out;
    }

    // Bell alert now (one unread alert per thread, not one per message), email at most daily.
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
