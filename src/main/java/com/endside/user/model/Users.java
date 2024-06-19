package com.endside.user.model;

import com.endside.user.constants.UserStatus;
import com.endside.user.constants.UserType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity(name = "users")
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile", nullable = false)
    private String mobile;

    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime loginAt;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime logoutAt;

    @Transient
    private LoginAddInfo loginAddInfo;


    @Builder
    public Users(long userId, UserStatus status, String password, String email, String mobile, UserType userType) {
        this.userId = userId;
        this.status = status;
        this.password = password;
        this.email = email;
        this.mobile = mobile;
        this.userType = userType;
    }
}
