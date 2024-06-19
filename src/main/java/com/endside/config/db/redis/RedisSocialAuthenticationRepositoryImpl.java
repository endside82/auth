package com.endside.config.db.redis;

import com.endside.social.vo.SocialProviderVerifyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;


@Repository
@Slf4j
public class RedisSocialAuthenticationRepositoryImpl {

    private final long KEY_EXPIRED_SECONDS = 3600;

    private final RedisTemplate<String, SocialProviderVerifyVo> socialRedisTemplate;
    private final ValueOperations<String, SocialProviderVerifyVo> setOperations;

    public RedisSocialAuthenticationRepositoryImpl(@Qualifier("socialRedisTemplate") RedisTemplate<String, SocialProviderVerifyVo> socialRedisTemplate) {
        this.socialRedisTemplate = socialRedisTemplate;
        this.socialRedisTemplate.setKeySerializer(new StringRedisSerializer());
        this.socialRedisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(SocialProviderVerifyVo.class));
        this.setOperations = socialRedisTemplate.opsForValue();
    }

    public void setValidationCode(String validationCode, SocialProviderVerifyVo socialProviderVerifyVo) {
        setOperations.set(validationCode, socialProviderVerifyVo, KEY_EXPIRED_SECONDS, TimeUnit.SECONDS);
    }

    public SocialProviderVerifyVo getValidationCode(String validationCode) {
        return setOperations.get(validationCode);
    }

    public void deleteValidationCode(String validationCode) {
        socialRedisTemplate.delete(validationCode);
    }

}
