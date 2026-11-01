package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Per-account messaging settings: the "Show when I'm active" switch, and when
// we last emailed this person about unread messages (at most once a day).
@Entity
@Table(name = "messaging_prefs")
@Getter
@Setter
@NoArgsConstructor
public class MessagingPrefs {

    @Id
    private Long userAccountId;

    private boolean showActive = true;

    private LocalDateTime lastMessageEmailAt;
}
