package com.endside.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity(name="agreement")
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "agree_term")
    private int agreeTerm;

    @Column(name = "agree_privacy")
    private int agreePrivacy;

    @Column(name = "agree_parent_alarm")
    private int agreeParentAlarm;

    @Column(name = "agree_marketing")
    private int agreeMarketing;

    @Column(name = "marketing_modified_at")
    private LocalDateTime marketingModifiedAt;

    @Column(name = "parent_alarm_modified_at")
    private LocalDateTime parentAlarmModifiedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
