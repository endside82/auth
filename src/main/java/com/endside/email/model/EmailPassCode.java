package com.endside.email.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RedisHash("authEmailCode")
public class EmailPassCode {
    // otp
    public EmailPassCode(String id, int otp, long ttl){
        this.id = id;
        this.otp = otp;
        this.timeToLive = ttl;
    }
    // cnt
    public EmailPassCode(String id, long ttl){
        this.id = id;
        this.cnt = 1;
        this.timeToLive = ttl;
    }

    @Id
    String id;
    int otp;
    int cnt;
    Date sendDate;
    @TimeToLive(unit = TimeUnit.MINUTES)
    long timeToLive;


}
