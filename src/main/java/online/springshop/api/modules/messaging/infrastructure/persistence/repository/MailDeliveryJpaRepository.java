package online.springshop.api.modules.messaging.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MailDeliveryEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MailDeliveryJpaRepository extends JpaRepository<MailDeliveryEntity, UUID>
{
    Optional<MailDeliveryEntity> findByProviderId (UUID providerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mde FROM MailDeliveryEntity mde WHERE mde.id = :id")
    Optional<MailDeliveryEntity> findByIdAndLock (UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mde FROM MailDeliveryEntity mde WHERE mde.providerId = :providerId")
    Optional<MailDeliveryEntity> findByProviderIdAndLock (UUID providerId);

    @Modifying
    @Query(value =
        """
        DELETE FROM mail_deliveries WHERE id IN (
            SELECT id FROM mail_deliveries
            WHERE id = :id
            FOR UPDATE SKIP LOCKED
        )
        """,
        nativeQuery = true
    )
    void deleteById (@NonNull UUID id);

    @Modifying
    @Query(value =
        """
        DELETE FROM mail_deliveries WHERE provider_id IN (
            SELECT provider_id FROM mail_deliveries
            WHERE provider_id = :providerId
            FOR UPDATE SKIP LOCKED
        )
        """,
        nativeQuery = true
    )
    void deleteByProviderId (UUID providerId);
}
