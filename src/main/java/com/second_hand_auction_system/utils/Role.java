package com.second_hand_auction_system.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.second_hand_auction_system.utils.Permission.*;

@Getter
@RequiredArgsConstructor
public enum Role {
    SELLER(
            Set.of(
                    SELLER_VIEW,
                    SELLER_CREATE,
                    SELLER_DELETE,
                    SELLER_UPDATE

            )
    ),
    BUYER(
            Set.of(
                    BUYER_VIEW,
                    BUYER_CREATE,
                    BUYER_DELETE,
                    BUYER_UPDATE

            )
    ),

    ADMIN(
            Set.of(
                    ADMIN_VIEW,
                    ADMIN_CREATE,
                    ADMIN_UPDATE,
                    ADMIN_DELETE

            )
    ),
    STAFF(
            Set.of(
                    STAFF_VIEW,
                    STAFF_CREATE,
                    STAFF_DELETE,
                    STAFF_UPDATE

            )
    );


    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
