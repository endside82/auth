package com.endside.user.repository;

import com.endside.user.model.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
    Optional<SocialLogin> findSocialLoginBySocialId(String socialId);
    void deleteByUserId(long userId);
}
