package com.endside.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity(name="jwt_record")
@AllArgsConstructor
@NoArgsConstructor
public class JwtRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long issueNo;
    @Column
    private long userId;
    @Column
    private long refreshTokenId;
    @Column
    private String ipAddress;
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime expireDatetime;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime logoutAt;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public JwtRecord(long userId, long refreshTokenId, String ipAddress, LocalDateTime expireDatetime) {
        this.userId = userId;
        this.refreshTokenId = refreshTokenId;
        this.ipAddress = ipAddress;
        this.expireDatetime = expireDatetime;
    }
}
