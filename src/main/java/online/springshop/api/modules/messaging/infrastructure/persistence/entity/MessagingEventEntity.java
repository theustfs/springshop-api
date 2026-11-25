package online.springshop.api.modules.messaging.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import online.springshop.api.modules.messaging.domain.event.MessagingEventStatus;

import java.time.Instant;
import java.util.UUID;

/** JPA representation of a messaging event's id, lifecycle status, and timestamps. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messaging_events")
public class MessagingEventEntity
{
    @Id
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private MessagingEventStatus status;

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
