package com.endside.social.service;

import com.endside.user.constants.ProviderType;
import com.endside.social.vo.SocialProviderVerifyVo;
import com.endside.config.db.redis.RedisSocialAuthenticationRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SocialValidationService {

    private final RedisSocialAuthenticationRepositoryImpl redisSSOAuthenticationRepository;

    public String setValidationCode(ProviderType providerType, String providerId, String socialEmail) {
        String validationCode = createValidationCode(providerId);
        SocialProviderVerifyVo socialProviderVerifyVo = SocialProviderVerifyVo.builder()
                .providerType(providerType)
                .providerId(providerId)
                .email(socialEmail)
                .build();
        redisSSOAuthenticationRepository.setValidationCode(validationCode, socialProviderVerifyVo);
        return validationCode;
    }

    private String createValidationCode(String providerId) {
        return providerId + ":" + generateSSOValidationCode();
    }

    private String generateSSOValidationCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    public SocialProviderVerifyVo getValidationCode(String validationCode) {
        return redisSSOAuthenticationRepository.getValidationCode(validationCode);
    }

    public void deleteValidationCode(String validationCode) {
        redisSSOAuthenticationRepository.deleteValidationCode(validationCode);
    }
}
