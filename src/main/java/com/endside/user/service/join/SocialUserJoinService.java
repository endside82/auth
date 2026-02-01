package com.endside.user.service.join;

import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.JoinFailureException;
import com.endside.config.error.exception.RestException;
import com.endside.social.service.SocialValidationService;
import com.endside.social.vo.SocialProviderVerifyVo;
import com.endside.social.vo.TossIdentificationResultVo;
import com.endside.user.constants.LoginType;
import com.endside.user.event.JoinEvent;
import com.endside.user.model.IdentityVerification;
import com.endside.user.model.SocialLogin;
import com.endside.user.model.UserSimple;
import com.endside.user.model.Users;
import com.endside.user.repository.IdentityVerificationRepository;
import com.endside.user.repository.SocialLoginRepository;
import com.endside.util.event.Events;
import com.endside.social.service.TossVerificationService;
import com.endside.user.param.SocialUserJoinParam;
import com.endside.user.param.UserJoinParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class SocialUserJoinService {
    private final SocialLoginRepository socialLoginRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final SocialValidationService socialValidationService;
    private final UserJoinCommonService userJoinCommonService;
    private final TossVerificationService tossVerificationService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");


    public SocialUserJoinService(
            SocialLoginRepository socialLoginRepository, IdentityVerificationRepository identityVerificationRepository, SocialValidationService socialValidationService, UserJoinCommonService userJoinCommonService, TossVerificationService tossVerificationService) {
        this.socialLoginRepository = socialLoginRepository;
        this.identityVerificationRepository = identityVerificationRepository;
        this.socialValidationService = socialValidationService;
        this.userJoinCommonService = userJoinCommonService;
        this.tossVerificationService = tossVerificationService;
    }

    /**
     * 회원가입 소셜로그인 기반
     */
    @Transactional(rollbackFor = {Exception.class})
    public UserSimple joinUser(SocialUserJoinParam socialUserJoinParam) {
        // 입력값 확인
        checkParameter(socialUserJoinParam);
        // 소셜 로그인 인증 정보 확인
        SocialProviderVerifyVo socialProviderInfo = confirmProvider(socialUserJoinParam.getValidationCode());
        // 본인인증 정보 확인
        TossIdentificationResultVo tossIdentificationResultVo = verifyIdentity(socialUserJoinParam.getTxId());
        // 유저 생성 파라미터 정보 생성
        UserJoinParam userJoinParam = generateJoinParam(socialProviderInfo, tossIdentificationResultVo);
        // 유저 생성 파라미터 더미값
        setDummyValue(userJoinParam, socialUserJoinParam);
        // 유저 생성
        UserSimple userSimple = addUser(userJoinParam);
        // 소셜 로그인 정보 생성
        addSocialLogin(socialProviderInfo, userSimple.getUserId());
        // 본인 인증 정보 생성
        addIdentityVerification(tossIdentificationResultVo.getCi(), userSimple.getUserId());
        // 인증 정보
        deleteVerification(socialUserJoinParam.getValidationCode());
        return userSimple;
    }

    private void addIdentityVerification(String ci, long userId) {
        identityVerificationRepository.save(IdentityVerification.builder()
                .ci(ci)
                .userId(userId)
                .build());
    }

    private void addSocialLogin(SocialProviderVerifyVo socialProviderInfo, long userId) {
        socialLoginRepository.save(SocialLogin.builder()
                .providerType(socialProviderInfo.getProviderType())
                .socialId(socialProviderInfo.getProviderId())
                .userId(userId)
                .build());

    }

    private UserJoinParam generateJoinParam(SocialProviderVerifyVo socialProviderInfo, TossIdentificationResultVo verifyResult) {
        UserJoinParam userJoinParam = new UserJoinParam();
        userJoinParam.setMobile(verifyResult.getPhone());
        userJoinParam.setEmail(socialProviderInfo.getEmail());
        userJoinParam.setBirthDate(LocalDate.parse(verifyResult.getBirthday(), formatter));
        return userJoinParam;
    }


    private void setDummyValue(UserJoinParam userJoinParam, SocialUserJoinParam socialUserJoinParam) {
        userJoinParam.setOs(socialUserJoinParam.getOs());
        userJoinParam.setUniqueId(socialUserJoinParam.getUniqueId());
        userJoinParam.setPassword(UUID.randomUUID().toString());
        userJoinParam.setVersion("0.1");
        userJoinParam.setLoginType(LoginType.SOCIAL);
        userJoinParam.setAgreeMarketing(socialUserJoinParam.isAgreeMarketing());
        userJoinParam.setAgreeParentAlarm(false);
        // email could be empty
        if (userJoinParam.getEmail() == null) {
            userJoinParam.setEmail("");
        }
        // mobile could be empty
        if (userJoinParam.getMobile() == null) {
            userJoinParam.setMobile("");
        }
    }

    public void checkParameter(SocialUserJoinParam socialUserJoinParam) {
        if (socialUserJoinParam.getValidationCode() == null || socialUserJoinParam.getValidationCode().isBlank()) {
            throw new RestException(ErrorCode.FAILED_TO_JOIN_NO_SOCIAL_VALIDATION_CODE);
        }
    }

    // 소셜 인증 확인
    private SocialProviderVerifyVo confirmProvider(String validationCode) {
        SocialProviderVerifyVo socialProviderVerifyVo = socialValidationService.getValidationCode(validationCode);
        if (socialProviderVerifyVo == null) {
            throw new JoinFailureException(ErrorCode.FAILED_TO_JOIN_VALIDATION_CODE_NOT_EXISTS);
        }
        // 중복 가입 방지 - 소셜 가입 정보 확인
        socialLoginRepository.findSocialLoginBySocialId(socialProviderVerifyVo.getProviderId())
                .ifPresent(socialLogin -> {
                    throw new JoinFailureException(ErrorCode.FAILED_TO_JOIN_ALREADY_JOIN_USER);
                });
        return socialProviderVerifyVo;
    }

    // 본인인증 정보 확인
    private TossIdentificationResultVo verifyIdentity(String txId) {
        return tossVerificationService.verifyIdentification(txId);
    }


    private void deleteVerification(String validationCode) {
        socialValidationService.deleteValidationCode(validationCode);
    }

    public UserSimple addUser(UserJoinParam userJoinParam) {
        // Add new User to DB
        Users newUsers = userJoinCommonService.saveUser(userJoinParam);
        if (newUsers == null) {
            throw new JoinFailureException(ErrorCode.FAILED_TO_JOIN_SAVE_USER);
        }
        // Set userId
        long userId = newUsers.getUserId();
        userJoinParam.setUserId(userId);
        // Add new device to DB
        userJoinCommonService.saveDevice(userJoinParam);
        // Add agreement
        userJoinCommonService.saveAgreement(userId, userJoinParam.isAgreeMarketing(), userJoinParam.isAgreeParentAlarm());
        // 후처리 이벤트
        Events.raise(new JoinEvent(userId));
        return UserSimple.builder()
                .userId(userId)
                .email(newUsers.getEmail())
                .loginType(userJoinParam.getLoginType())
                .build();
    }

}