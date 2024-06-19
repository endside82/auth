package com.endside.config.redis;

import com.endside.social.vo.SocialProviderVerifyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;


/**
 * Redis Connection 및 캐시 관리 설정
 */
@Slf4j
@EnableCaching(proxyTargetClass = true)
@Configuration
public class RedisConfiguration {

    @Value("${redis.host}")
    private String host;
    @Value("${redis.port:6379}")
    private int port;
    @Value("${redis.authentication.database:0}")
    private int authDatabase;
    @Value("${redis.user.database:1}")
    private int userDatabase;
    @Value("${redis.noti.database}")
    private int notiDatabase;
    @Value("${redis.sso.database}")
    private int socialDatabase;

    @Value("${redis.password:bg}")
    private String password;
    @Value("${redis.auth:true}")
    private boolean redisAuth;
    @Value("${redis.type:STANDARD}")
    private String redisType;

    @Primary
    @Qualifier("authRedisConnectionFactory")
    @Bean(name="authRedisConnectionFactory")
    LettuceConnectionFactory authRedisConnectionFactory() {
        return getLettuceConnectionFactory(authDatabase);
    }

    @Qualifier("userRedisConnectionFactory")
    @Bean(name="userRedisConnectionFactory")
    public RedisConnectionFactory userRedisConnectionFactory() {
        return getLettuceConnectionFactory(userDatabase);
    }

    private LettuceConnectionFactory getLettuceConnectionFactory(int DBnum) {
        if (redisType.compareTo("CLUSTER") == 0) {
            // clustering 구성 config
            RedisClusterConfiguration redisClusterConfiguration = getRedisClusterConf();
            LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory(redisClusterConfiguration);
            redisConnectionFactory.setDatabase(DBnum);
            return redisConnectionFactory;
        }
        // else Redis is standard
        RedisStandaloneConfiguration redisStandaloneConfiguration = getRedisStandardConf(DBnum);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }


    @Qualifier("authRedisTemplate")
    @Bean(name="authRedisTemplate")
    public RedisTemplate<String, Object> authRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(authRedisConnectionFactory());
        return redisTemplate;
    }

    @Qualifier("userRedisTemplate")
    @Bean(name="userRedisTemplate")
    public RedisTemplate<String, String> userRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(userRedisConnectionFactory());
        return redisTemplate;
    }

    @Qualifier("notiRedisConnectionFactory")
    @Bean(name="notiRedisConnectionFactory")
    LettuceConnectionFactory notiRedisConnectionFactory() {
        return getLettuceConnectionFactory(notiDatabase);
    }

    @Qualifier("notiRedisTemplate")
    @Bean(name="notiRedisTemplate")
    public RedisTemplate<String, String>  notiRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(notiRedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }

    @Qualifier("socialRedisConnectionFactory")
    @Bean(name="socialRedisConnectionFactory")
    LettuceConnectionFactory ssoRedisConnectionFactory() {
        return getLettuceConnectionFactory(socialDatabase);
    }

    @Qualifier("socialRedisTemplate")
    @Bean(name = "socialRedisTemplate")
    public RedisTemplate<String, SocialProviderVerifyVo> socialRedisTemplate() {
        RedisTemplate<String, SocialProviderVerifyVo> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(ssoRedisConnectionFactory());
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }

    private RedisStandaloneConfiguration getRedisStandardConf(int dbNum) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (redisAuth) {
            config.setPassword(password);
        }
        config.setDatabase(dbNum);
        return config;
    }

    private RedisClusterConfiguration getRedisClusterConf() {
        List<String> nodes = Collections.singletonList(host + ":" + port);
        return new RedisClusterConfiguration(nodes);
    }


    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(authRedisConnectionFactory());
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .prefixCacheNameWith("cache:")
                .entryTtl(Duration.ofHours(24L));
        builder.cacheDefaults(configuration);
        return builder.build();
    }
}
