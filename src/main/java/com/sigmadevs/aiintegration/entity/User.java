package com.sigmadevs.aiintegration.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigmadevs.aiintegration.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_token")
    private String loginToken;
    @Column(name = "main_token")
    private String mainToken;
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Email
    private String email;

//    @ToString.Exclude
//    @JsonIgnore
//    @Column(nullable = false)
//    private String password;

    @Column(nullable = false)
    private String image;

//    @Column(nullable = false,name = "is_email_verified")
//    private Boolean isEmailVerified;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private Role role = Role.USER;






    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(this.role);
    }

    @Override
    public String getPassword() {
        return "";
//        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}