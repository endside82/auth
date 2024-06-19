package com.endside.user.repository;

import com.endside.user.model.IdentityVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, Long> {
    void deleteByUserId(long userId);
}
