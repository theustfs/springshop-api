package online.springshop.api.modules.messaging.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mail_deliveries")
public class MailDeliveryEntity
{
    @Id
    @Setter(AccessLevel.NONE)
    private UUID id;
    private UUID providerId;
    @Enumerated(EnumType.STRING)
    private MailDeliveryStatus status;

    @Column(updatable = false)
    private Instant createdAt;
    private Instant statusChangedAt;
    private Instant providerStatusChangedAt;

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof MailDeliveryEntity that))
            return false;

        return id.equals(that.id);
    }

    @Override
    public int hashCode ()
    {
        return id.hashCode();
    }
}
