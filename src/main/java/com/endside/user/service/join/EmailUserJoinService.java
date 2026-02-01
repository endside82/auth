package com.endside.user.service.join;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.endside.user.constants.LoginType;
import com.endside.user.model.Users;
import com.endside.user.param.UserJoinParam;
import com.endside.user.vo.UserSimpleVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailUserJoinService implements UserJoinService {
    private final UserJoinCommonService userJoinCommonService;
    private final UserJoinVerificationService userJoinVerificationService;
    private final JoinHelperService joinHelperService;
    @Value("${email.verify:false}")
    private boolean EMAIL_VERIFY;

    /**
     * 회원가입 이메일 인증 기반
     * 입력값 체크(이메일 형식 검사)
     * 중복 이메일 가입 방지
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public UserSimpleVo joinUser(UserJoinParam userJoinParam) {
        // 더미 값 세팅
        setDummyValue(userJoinParam);
        // 파라미터 체크
        checkParameter(userJoinParam);
        // 이메일 검사설정이 있으면
        if (EMAIL_VERIFY) {
            userJoinVerificationService.emailVerify(userJoinParam.getUniqueId(), userJoinParam.getEmail());
        }
        Users user = userJoinCommonService.addUser(userJoinParam);
        // 이메일 검사설정이 있으면
        if (EMAIL_VERIFY) {
            userJoinVerificationService.deleteEmailVerification(userJoinParam.getUniqueId(), userJoinParam.getEmail());
        }
        return UserSimpleVo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .loginType(userJoinParam.getLoginType())
                .build();
    }

    // 더미 값 세팅
    private void setDummyValue(UserJoinParam userJoinParam) {
        userJoinParam.setOs(userJoinParam.getOs());
        userJoinParam.setVersion("0.1");
        userJoinParam.setLoginType(LoginType.EMAIL);
        userJoinParam.setLoginId(joinHelperService.generateAlphanumericRandomString(20));
        // mobile could be empty
        if (userJoinParam.getMobile() == null) {
            userJoinParam.setMobile("");
        }
    }

    // 파라미터 유효성 검사
    public void checkParameter(UserJoinParam userJoinParam) {
        userJoinVerificationService.checkEmailRegex(userJoinParam.getEmail());
        // 중복 email 가입 방지
        userJoinCommonService.checkHasSameEmail(userJoinParam.getEmail());
        // 중복 전화번호 가입 방지
        if (userJoinParam.getMobile() != null && !userJoinParam.getMobile().isEmpty()) {
            userJoinCommonService.checkHasSameMobile(userJoinParam.getMobile());
        }
    }
}
