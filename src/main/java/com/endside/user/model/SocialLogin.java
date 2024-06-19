package com.endside.user.model;

import com.endside.user.constants.ProviderType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="social_login")
@NoArgsConstructor
public class SocialLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private Long userId;
    private String socialId;
    private ProviderType providerType;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public SocialLogin( String socialId, Long userId, ProviderType providerType) {
        this.socialId = socialId;
        this.userId = userId;
        this.providerType = providerType;
    }
}
