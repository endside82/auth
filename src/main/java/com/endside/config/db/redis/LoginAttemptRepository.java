package com.endside.config.db.redis;

import com.endside.config.redis.LoginAttempt;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository extends CrudRepository<LoginAttempt,String > {

}
