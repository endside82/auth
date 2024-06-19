package com.endside.user.model;

import com.endside.user.constants.Os;
import lombok.Data;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name=RefreshToken.TABLE_NAME)
public class RefreshToken {
    public static final String TABLE_NAME= "refresh_token";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    private Os os;

    private String refreshToken;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime expireDatetime;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

}
