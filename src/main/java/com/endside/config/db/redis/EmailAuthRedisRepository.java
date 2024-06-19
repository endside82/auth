package com.endside.config.db.redis;

import com.endside.email.model.EmailPassCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailAuthRedisRepository extends CrudRepository<EmailPassCode, String> {
    Optional<EmailPassCode> findById(String id);
    void deleteById(String id);
}
