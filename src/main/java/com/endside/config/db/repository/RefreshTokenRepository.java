package com.endside.config.db.repository;

import com.endside.user.constants.Os;
import com.endside.user.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Transactional
    void deleteAllByUserId(long user_Id);

    void deleteById(long refreshTokenId);

    Optional<RefreshToken> findByUserIdAndRefreshToken(long userId, String refreshToken);

    List<RefreshToken> findByUserIdAndOsAndExpireDatetimeGreaterThanEqual(long userId, Os os, LocalDateTime now);

    void deleteAllByUserIdAndOs(long userId, Os os);
}
