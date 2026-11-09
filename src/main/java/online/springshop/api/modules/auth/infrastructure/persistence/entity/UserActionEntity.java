package online.springshop.api.modules.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA representation of a user action and the ordered challenges it owns.
 *
 * <p>The database permits at most one row per account. Challenge order is
 * stored explicitly and restored through {@link OrderBy}; adding a challenge
 * also establishes the owning side of the bidirectional association.</p>
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users_actions")
public class UserActionEntity
{
    public UserActionEntity (
        UUID id,
        UUID accountId,
        ActionType actionType
    )
    {
        this.id = id;
        this.accountId = accountId;
        this.actionType = actionType;
    }

    @Id
    private UUID id;

    private UUID accountId;

    @Enumerated(value = EnumType.STRING)
    private ActionType actionType;

    @OrderBy("challengeOrder ASC")
    @OneToMany(mappedBy = "userActionEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfirmationChallengeEntity> challenges = new ArrayList<>();

    /**
     * Appends a challenge, assigns its zero-based order and associates it with
     * this action.
     *
     * @param challenge challenge to append
     */
    public void addChallenge (ConfirmationChallengeEntity challenge)
    {
        if (challenge == null)
            throw new NullPointerException("Challenge must not be null");

        challenge.setChallengeOrder(challenges.size());
        challenges.add(challenge);
        challenge.setUserActionEntity(this);
    }
}
