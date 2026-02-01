package com.endside.user.service.join;

import lombok.RequiredArgsConstructor;
import com.endside.user.constants.LoginType;
import com.endside.user.vo.UserSimpleVo;
import com.endside.user.model.Users;
import com.endside.user.param.UserJoinParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileUserJoinService implements UserJoinService {

    private final UserJoinCommonService userJoinCommonService;
    private final UserJoinVerificationService userJoinVerificationService;
    private final JoinHelperService joinHelperService;

    @Value("${sms.verify:false}")
    private boolean SMS_VERIFY;

    /**
     * 회원가입 모바일 인증 기반
     * 입력값 체크(모바일 형식 검사)
     * 중복 모바일 가입 방지
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public UserSimpleVo joinUser(UserJoinParam userJoinParam) {
        setDummyValue(userJoinParam);
        checkParameter(userJoinParam);
        if (SMS_VERIFY) {
            userJoinVerificationService.mobileVerify(userJoinParam.getUniqueId(), userJoinParam.getMobile());
        }
        Users user = userJoinCommonService.addUser(userJoinParam);
        if (SMS_VERIFY) {
            userJoinVerificationService.deleteMobileVerification(userJoinParam.getUniqueId(), userJoinParam.getMobile());
        }
        return UserSimpleVo.builder()
                .userId(user.getUserId())
                .mobile(user.getMobile())
                .loginType(userJoinParam.getLoginType())
                .build();
    }

    private void setDummyValue(UserJoinParam userJoinParam) {
        userJoinParam.setOs(userJoinParam.getOs());
        userJoinParam.setVersion("0.1");
        userJoinParam.setLoginType(LoginType.MOBILE);
        userJoinParam.setLoginId(joinHelperService.generateAlphanumericRandomString(20));
        // email could be empty
        if (userJoinParam.getEmail() == null) {
            userJoinParam.setEmail("");
        }
    }

    // 파라미터 유효성 검사
    public void checkParameter(UserJoinParam userJoinParam) {
        userJoinVerificationService.checkMobileRegex(userJoinParam.getMobile());
        // 중복 전화번호 가입 방지
        userJoinCommonService.checkHasSameMobile(userJoinParam.getMobile());
    }
}
