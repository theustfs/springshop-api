package online.springshop.api.modules.auth.infrastructure.persistence.mapper;

import online.springshop.api.modules.auth.domain.useraction.ConfirmationChallenge;
import online.springshop.api.modules.auth.domain.useraction.UserAction;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.ConfirmationChallengeEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.UserActionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps complete user-action aggregates between the domain and JPA models.
 *
 * <p>Mapping to persistence rebuilds both sides of the action/challenge
 * association and assigns challenge positions in domain-list order. Mapping to
 * the domain restores challenge state from trusted persisted values.</p>
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserActionMapper
{
    /** Restores a domain aggregate from a fully initialized persistence graph. */
    default UserAction toDomain (UserActionEntity entity)
    {
        if (entity == null)
            return null;

        List<ConfirmationChallenge> challenges = new ArrayList<>(entity.getChallenges().size());

        for (ConfirmationChallengeEntity c : entity.getChallenges())
            challenges.add(ConfirmationChallenge.restore(
                c.getId(),
                EncodedConfirmationCode.from(c.getEncodedConfirmationCode()),
                c.getAttempts(),
                c.isCompleted(),
                c.getExpiresAt()
            ));

        return UserAction.restore(
            entity.getId(),
            entity.getAccountId(),
            entity.getActionType(),
            challenges
        );
    }

    /** Creates a persistence graph from a domain aggregate. */
    default UserActionEntity toEntity (UserAction domain)
    {
        if (domain == null)
            return null;

        UserActionEntity entity = new UserActionEntity(
            domain.getId(),
            domain.getAccountId(),
            domain.getActionType()
        );

        for (ConfirmationChallenge challenge : domain.getChallenges())
        {
            ConfirmationChallengeEntity confirmationChallengeEntity = new ConfirmationChallengeEntity(
                challenge.getId(),
                challenge.getEncodedConfirmationCode().getValue(),
                challenge.getAttempts(),
                challenge.isCompleted(),
                challenge.getExpiresAt(),
                null,
                null
            );

            entity.addChallenge(confirmationChallengeEntity);
        }

        return entity;
    }
}
