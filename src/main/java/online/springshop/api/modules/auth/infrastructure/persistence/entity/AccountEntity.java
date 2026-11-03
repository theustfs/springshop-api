package online.springshop.api.modules.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import online.springshop.api.modules.auth.domain.account.Role;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class AccountEntity
{
    @Id
    @Setter(AccessLevel.NONE)
    private UUID id;

    private String email;
    private String encodedPassword;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(updatable = false)
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletionScheduledAt;

    private boolean verified;

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof AccountEntity that))
            return false;

        UUID thisId = getId();
        UUID thatId = that.getId();

        return thisId != null && thisId.equals(thatId);
    }

    @Override
    public int hashCode ()
    {
        UUID currentId = getId();

        return currentId != null ? currentId.hashCode() : 0;
    }
}
