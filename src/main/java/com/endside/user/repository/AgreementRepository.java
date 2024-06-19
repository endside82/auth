package com.endside.user.repository;

import com.endside.user.model.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    void deleteByUserId(long userId);
}
