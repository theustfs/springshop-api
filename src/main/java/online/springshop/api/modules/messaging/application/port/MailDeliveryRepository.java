package online.springshop.api.modules.messaging.application.port;

import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryId;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;

import java.util.Optional;

/**
 * Application-facing persistence contract for {@link MailDelivery} aggregates.
 *
 * <p>Locking lookups acquire a database write lock and must be invoked inside an
 * application-service transaction. The lock remains effective until that surrounding
 * transaction completes.</p>
 *
 * <p>The delete methods should be used to remove terminal mail deliveries after their
 * retention requirements have been satisfied. They do not check the delivery status, so
 * application services are responsible for ensuring a delivery is terminal before deleting it.</p>
 */
public interface MailDeliveryRepository
{
    /** Saves a new aggregate or persists changes to an existing aggregate. */
    void save (MailDelivery mailDelivery);

    /** Finds an aggregate by its internal mail-delivery identifier. */
    Optional<MailDelivery> findById (MailDeliveryId id);

    /** Finds an aggregate by the identifier assigned by the mail provider. */
    Optional<MailDelivery> findByProviderId (MailProviderId providerId);

    /** Finds an aggregate by its internal identifier and acquires a pessimistic write lock. */
    Optional<MailDelivery> findByIdAndLock (MailDeliveryId id);

    /** Finds an aggregate by its provider identifier and acquires a pessimistic write lock. */
    Optional<MailDelivery> findByProviderIdAndLock (MailProviderId providerId);

    /** Deletes an aggregate by its internal mail-delivery identifier;*/
    void delete (MailDeliveryId id);

    /** Deletes an aggregate by its provider identifier;*/
    void delete (MailProviderId providerId);
}
