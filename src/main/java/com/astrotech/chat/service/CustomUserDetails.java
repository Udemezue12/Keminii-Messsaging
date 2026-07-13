package com.astrotech.chat.service;

import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {


    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        var roleName = "ROLE_" + user.getRole().name();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.BANNED && user.getStatus() != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null;
    }
}