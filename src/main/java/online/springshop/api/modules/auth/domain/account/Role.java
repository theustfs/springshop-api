package online.springshop.api.modules.auth.domain.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role
{
    USER("ROLE_USER"),
    PRODUCT_MANAGER("ROLE_PRODUCT_MANAGER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;
}
