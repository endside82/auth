package com.endside.user.service.join;

import com.endside.config.db.redis.EmailAuthRedisRepository;
import com.endside.config.db.redis.SmsAuthRedisRepository;
import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.InvalidParameterException;
import com.endside.config.error.exception.JoinFailureException;
import com.endside.email.constants.EmailType;
import com.endside.email.model.EmailPassCode;
import com.endside.util.RedisKeyGen;
import com.endside.sms.constants.SmsType;
import com.endside.sms.model.SmsPassCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserJoinVerificationService {
    private static final String EMAIL_REG_EX = "^(.+)@(.+)$";
    private final EmailAuthRedisRepository emailAuthRedisRepository;
    private final SmsAuthRedisRepository smsAuthRedisRepository;
    private final RedisKeyGen redisKeyGen;

    private static final String MOBILE_REG_EX = "(\\d+)";

    public void checkEmailRegex(String email) {
        if (email == null || !Pattern.matches(EMAIL_REG_EX, email)) {
            throw new InvalidParameterException(ErrorCode.EMAIL_REGEX_VALIDATION);
        }
    }

    public void checkMobileRegex(String mobile) {
        if (mobile == null || !Pattern.matches(MOBILE_REG_EX, mobile)) {
            throw new InvalidParameterException(ErrorCode.MOBILE_REGEX_VALIDATION);
        }
    }


    public void mobileVerify(String uniqueId, String mobile) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_JOIN.getStrCode(), uniqueId, mobile);
        log.debug("authCacheId : " + authCacheId);
        SmsPassCode smsPassCode = smsAuthRedisRepository.findById(authCacheId).orElseThrow(
                () -> new JoinFailureException(ErrorCode.FAILED_TO_JOIN_BY_INVALID_MOBILE_AUTH)
        );
        log.debug("smsPassCode : " + smsPassCode);
    }

    public void deleteMobileVerification(String uniqueId, String mobile) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_JOIN.getStrCode(), uniqueId, mobile);
        smsAuthRedisRepository.deleteById(authCacheId);
        String cntCacheId = redisKeyGen.generateKeyId(RedisKeyGen.COUNT, SmsType.OTP_TO_JOIN.getStrCode(), uniqueId, mobile);
        smsAuthRedisRepository.deleteById(cntCacheId);
    }

    public void emailVerify(String uniqueId, String email) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, EmailType.OTP_TO_JOIN.getStrCode(), uniqueId, email);
        log.debug("authCacheId : " + authCacheId);
        EmailPassCode emailPassCode = emailAuthRedisRepository.findById(authCacheId).orElseThrow(
                () -> new JoinFailureException(ErrorCode.FAILED_TO_JOIN_BY_INVALID_EMAIL_AUTH)
        );
        log.debug("emailPassCode : " + emailPassCode);
    }

    public void deleteEmailVerification(String uniqueId, String email) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, EmailType.OTP_TO_JOIN.getStrCode(), uniqueId, email);
        emailAuthRedisRepository.deleteById(authCacheId);
        String cntCacheId = redisKeyGen.generateKeyId(RedisKeyGen.COUNT, EmailType.OTP_TO_JOIN.getStrCode(), uniqueId, email);
        emailAuthRedisRepository.deleteById(cntCacheId);
    }


}
