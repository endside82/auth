package com.endside.user.service;

import com.endside.config.db.redis.EmailAuthRedisRepository;
import com.endside.config.db.redis.SmsAuthRedisRepository;
import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.EmailRestException;
import com.endside.config.error.exception.JoinFailureException;
import com.endside.config.error.exception.RestException;
import com.endside.user.model.Users;
import com.endside.user.repository.UserRepository;
import com.endside.util.RedisKeyGen;
import com.endside.user.param.PasswordEmailParam;
import com.endside.user.param.PasswordParam;
import com.endside.user.param.PasswordSmsParam;
import com.endside.sms.constants.SmsType;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailAuthRedisRepository emailAuthRedisRepository;
    private final SmsAuthRedisRepository smsAuthRedisRepository;
    private final RedisKeyGen redisKeyGen;

    public UserService(UserRepository userRepository, EmailAuthRedisRepository emailAuthRedisRepository,
                       SmsAuthRedisRepository smsAuthRedisRepository, RedisKeyGen redisKeyGen) {
        this.userRepository = userRepository;
        this.emailAuthRedisRepository = emailAuthRedisRepository;
        this.smsAuthRedisRepository = smsAuthRedisRepository;
        this.redisKeyGen = redisKeyGen;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    // 비로그인 상태에서 이메일로 인증한 토큰으로 패스워드 변경
    public void checkTokenByMailToUpdatePwd(PasswordEmailParam passwordEmailParam) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_PASSWORD.getStrCode(), passwordEmailParam.getToken(), passwordEmailParam.getEmail());
        emailAuthRedisRepository.findById(authCacheId).orElseThrow(
                () -> new JoinFailureException(ErrorCode.FAILED_TO_CHANGE_PW_INVALID_TOKEN)
        );
        Users users = userRepository.findByEmail(passwordEmailParam.getEmail()).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        );
        if (bCryptPasswordEncoder.matches(passwordEmailParam.getPassword(), users.getPassword())) {
            throw new EmailRestException(ErrorCode.SAME_CURRENT_PASSWORD);
        }
        String encodedPwd = bCryptPasswordEncoder.encode(passwordEmailParam.getPassword());
        users.setPassword(encodedPwd);
        userRepository.save(users);
        emailAuthRedisRepository.deleteById(authCacheId);
        // TODO 패스워드 변경 시도 횟수 초기화
        String cntCacheId = redisKeyGen.generateKeyId(RedisKeyGen.COUNT, SmsType.OTP_TO_PASSWORD.getStrCode(), String.valueOf(users.getUserId()), passwordEmailParam.getEmail());
        emailAuthRedisRepository.deleteById(cntCacheId);

    }

    // 비로그인 상태에서 SMS로 인증한 토큰으로 패스워드 변경
    public void checkTokenBySmsToUpdatePwd(PasswordSmsParam passwordSmsParam) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_PASSWORD.getStrCode(), passwordSmsParam.getToken(), passwordSmsParam.getMobile());
        smsAuthRedisRepository.findById(authCacheId).orElseThrow(
                () -> new JoinFailureException(ErrorCode.FAILED_TO_CHANGE_PW_INVALID_TOKEN)
        );
        Users users = userRepository.findByMobile(passwordSmsParam.getMobile()).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        );
        if (bCryptPasswordEncoder.matches(passwordSmsParam.getPassword(), users.getPassword())) {
            throw new EmailRestException(ErrorCode.SAME_CURRENT_PASSWORD);
        }
        String encodedPwd = bCryptPasswordEncoder.encode(passwordSmsParam.getPassword());
        users.setPassword(encodedPwd);
        userRepository.save(users);
        smsAuthRedisRepository.deleteById(authCacheId);
        // 패스워드 변경 시도 횟수 초기화
        String cntCacheId = redisKeyGen.generateKeyId(RedisKeyGen.COUNT, SmsType.OTP_TO_PASSWORD.getStrCode(), String.valueOf(users.getUserId()), passwordSmsParam.getMobile());
        smsAuthRedisRepository.deleteById(cntCacheId);
    }

    // 로그인 상태에서 패스워드 변경
    public void updatePwd(PasswordParam passwordParam) {
        Users users = userRepository.findByUserId(passwordParam.getUserId()).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        );
        if (StringUtil.isNullOrEmpty(passwordParam.getOldPassword()) || StringUtil.isNullOrEmpty(passwordParam.getNewPassword())) {
            throw new EmailRestException(ErrorCode.FAILED_TO_CHANGE_PW_REQUIRED_PARAMETER);
        }
        if (!bCryptPasswordEncoder.matches(passwordParam.getOldPassword(), users.getPassword())) {
            throw new EmailRestException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (passwordParam.getNewPassword().equals(passwordParam.getOldPassword())) {
            throw new EmailRestException(ErrorCode.SAME_CURRENT_PASSWORD);
        }
        String encodedPwd = bCryptPasswordEncoder.encode(passwordParam.getNewPassword());
        users.setPassword(encodedPwd);
        userRepository.save(users);
    }


}
