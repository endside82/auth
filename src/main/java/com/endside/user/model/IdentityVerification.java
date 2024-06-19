package com.endside.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity(name="identity_verification")
@NoArgsConstructor
public class IdentityVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id; // 본인인증 고유번호
    private Long userId; // 유저 고유번호
    private String ci; // 연계정보 고유 아이디
    @CreationTimestamp
    private LocalDateTime createdAt; // 생성일

    @Builder
    public IdentityVerification(Long userId, String ci) {
        this.userId = userId;
        this.ci = ci;
    }
}
