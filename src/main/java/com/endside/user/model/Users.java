package com.endside.user.model;

import com.endside.user.constants.LoginType;
import com.endside.user.constants.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    @Column(name = "login_id")
    private String loginId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile", nullable = false)
    private String mobile;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "init_at")
    private LocalDateTime initAt;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private LoginAddInfo loginAddInfo;
}
