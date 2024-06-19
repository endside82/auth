package com.endside.config.db.repository;

import com.endside.user.model.JwtRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JwtTokenIssueRecordRepository extends JpaRepository<JwtRecord, Long> {
    int deleteAllByUserId(long userId);
    List<JwtRecord> findAllByUserIdAndExpireDatetimeGreaterThanEqualAndLogoutAtIsNull(long userId, LocalDateTime date);
    List<JwtRecord> findAllByUserIdAndRefreshTokenIdAndExpireDatetimeGreaterThanEqualAndLogoutAtIsNull(long userId, long refreshTokenId, LocalDateTime now);
}
