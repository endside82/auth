package com.endside.user.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity(name="drop_out_user")
public class DropOutUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    @Column
    private String email;

    private byte[] mobile;
    @CreationTimestamp
    private LocalDateTime dropAt;

    @Builder
    public DropOutUser(long id, long userId, String email, byte[] mobile, LocalDateTime dropAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.mobile = mobile;
        this.dropAt = dropAt;
    }
}

