package online.springshop.api.modules.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Persisted state of one ordered confirmation step owned by a user action.
 *
 * <p>The database generates the identifier. {@code challengeOrder} is scoped
 * to the owning action and is populated by
 * {@link UserActionEntity#addChallenge(ConfirmationChallengeEntity)}.</p>
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "confirmation_challenges")
public class ConfirmationChallengeEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String encodedConfirmationCode;

    private int attempts;

    private boolean completed;

    private Instant expiresAt;

    private Integer challengeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_action_id")
    private UserActionEntity userActionEntity;
}
