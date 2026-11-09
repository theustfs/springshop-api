package online.springshop.api.modules.auth.infrastructure.persistence.mapper;

import com.fasterxml.uuid.Generators;
import online.springshop.api.modules.auth.domain.useraction.ConfirmationChallenge;
import online.springshop.api.modules.auth.domain.useraction.UserAction;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ActionType;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.ConfirmationChallengeEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.UserActionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User action mapper test")
class UserActionMapperTest
{
    private final UserActionMapper mapper = Mappers.getMapper(UserActionMapper.class);

    private static final UUID USER_ACTION_ID = Generators.timeBasedEpochGenerator().generate();
    private static final UUID ACCOUNT_ID = Generators.timeBasedEpochGenerator().generate();

    private String getRandomEncodedConfirmationCodeValue ()
    {
        return "encodedConfirmationCode%s".formatted((int) (Math.random() * Integer.MAX_VALUE));
    }

    @Nested
    @DisplayName("Domain to entity mapping tests")
    class DomainToEntityMappingTests
    {
        private ConfirmationChallenge getConfirmationChallengeDomain (
            Long id,
            Instant expiresAt
        )
        {
            return ConfirmationChallenge.restore(
                id,
                EncodedConfirmationCode.from(getRandomEncodedConfirmationCodeValue()),
                0,
                false,
                expiresAt
            );
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull ()
        {
            assertNull(mapper.toDomain(null));
        }

        @Test
        @DisplayName("Should map domain to entity")
        void shouldMapDomainToEntity ()
        {
            UserAction domain = UserAction.restore(
                USER_ACTION_ID,
                ACCOUNT_ID,
                ActionType.ACCOUNT_DELETION,
                List.of(
                    getConfirmationChallengeDomain(1L, Instant.now()),
                    getConfirmationChallengeDomain(2L, null)
                )
            );

            UserActionEntity entity = mapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(domain.getId(), entity.getId());
            assertEquals(domain.getAccountId(), entity.getAccountId());
            assertEquals(domain.getActionType(), entity.getActionType());

            for (int i=0; i < domain.getChallenges().size(); i++)
            {
                ConfirmationChallenge domainChallenge = domain.getChallenges().get(i);
                ConfirmationChallengeEntity entityChallenge = entity.getChallenges().get(i);

                assertEquals(domainChallenge.getId(), entityChallenge.getId());
                assertEquals(domainChallenge.getEncodedConfirmationCode().getValue(), entityChallenge.getEncodedConfirmationCode());
                assertEquals(domainChallenge.getAttempts(), entityChallenge.getAttempts());
                assertEquals(domainChallenge.isCompleted(), entityChallenge.isCompleted());
                assertEquals(domainChallenge.getExpiresAt(), entityChallenge.getExpiresAt());
                assertEquals(entity, entityChallenge.getUserActionEntity());
                assertEquals(i, entityChallenge.getChallengeOrder());
            }
        }
    }

    @Nested
    @DisplayName("Entity to domain mapping tests")
    class EntityToDomainMappingTests
    {
        private ConfirmationChallengeEntity getConfirmationChallengeEntity (
            Long id,
            Instant expiresAt
        )
        {
            return new ConfirmationChallengeEntity(
                id,
                getRandomEncodedConfirmationCodeValue(),
                0,
                false,
                expiresAt,
                null,
                null
            );
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNull ()
        {
            assertNull(mapper.toDomain(null));
        }

        @Test
        @DisplayName("Should map entity to domain")
        void shouldMapEntityToDomain ()
        {
            UserActionEntity entity = new UserActionEntity(
                USER_ACTION_ID,
                ACCOUNT_ID,
                ActionType.ACCOUNT_REGISTRATION,
                List.of(
                    getConfirmationChallengeEntity(1L, Instant.now()),
                    getConfirmationChallengeEntity(2L, null)
                )
            );

            UserAction domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(entity.getId(), domain.getId());
            assertEquals(entity.getAccountId(), domain.getAccountId());
            assertEquals(entity.getActionType(), domain.getActionType());

            for (int i=0; i < entity.getChallenges().size(); i++)
            {
                ConfirmationChallengeEntity entityChallenge = entity.getChallenges().get(i);
                ConfirmationChallenge domainChallenge = domain.getChallenges().get(i);

                assertEquals(entityChallenge.getId(), domainChallenge.getId());
                assertEquals(entityChallenge.getEncodedConfirmationCode(), domainChallenge.getEncodedConfirmationCode().getValue());
                assertEquals(entityChallenge.getAttempts(), domainChallenge.getAttempts());
                assertEquals(entityChallenge.isCompleted(), domainChallenge.isCompleted());
                assertEquals(entityChallenge.getExpiresAt(), domainChallenge.getExpiresAt());
            }
        }
    }
}