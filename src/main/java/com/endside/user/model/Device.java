package com.endside.user.model;

import com.endside.user.constants.Os;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Entity(name="device")
@NoArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    @Column(name = "user_id", nullable = false)
    private long userId;
    @Column(name = "unique_id", nullable = false)
    private String uniqueId;
    @Column(name = "version", nullable = false)
    private String version;
    @Column(name = "os", nullable = false)
    private Os os;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Device(long userId, String version, Os os, String uniqueId){
        this.userId = userId;
        this.version = version;
        this.os = os;
        this.uniqueId = uniqueId;
    }

}
