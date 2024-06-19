package com.endside.config.db.redis;

import com.endside.sms.model.SmsPassCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@SuppressWarnings("ALL")
@Repository
public interface SmsAuthRedisRepository extends CrudRepository<SmsPassCode, String> {
    @SuppressWarnings("NullableProblems")
    Optional<SmsPassCode> findById(String id);
    @SuppressWarnings("NullableProblems")
    void deleteById(String id);
}
