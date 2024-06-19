package com.endside.user.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity(name="agreement")
public class Agreement {
    @Builder
    public Agreement(long userId, boolean agreeMarketing) {
        this.agreeTerm = true;
        this.agreePrivacy = true;
        this.agreeSensitive = true;
        this.userId = userId;
        this.agreeMarketing = agreeMarketing;
        this.marketingModifiedAt = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    private boolean agreeTerm;

    private boolean agreePrivacy;

    private boolean agreeMarketing;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime marketingModifiedAt;

    private boolean agreeSensitive;

    @CreationTimestamp
    private LocalDateTime createdAt;



}
