package com.endside.user.service.join;


import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.InvalidParameterException;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.model.UserSimple;
import com.endside.user.model.Users;
import com.endside.user.param.UserJoinParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
public class EmailUserJoinService implements UserJoinService {
    private final static String EMAIL_REG_EX = "^(.+)@(.+)$";
    private final UserJoinCommonService userJoinCommonService;
    private final UserJoinVerificationService userJoinVerificationService;

    public EmailUserJoinService(UserJoinCommonService userJoinCommonService, UserJoinVerificationService userJoinVerificationService) {
        this.userJoinCommonService = userJoinCommonService;
        this.userJoinVerificationService = userJoinVerificationService;
    }

    /**
     * (WEB) 회원가입 이메일 인증 기반
     * 입력값 체크(이메일 형식 검사)
     * 중복 이메일 가입 방지
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public UserSimple joinUser(UserJoinParam userJoinParam) {
        setDummyValue(userJoinParam);
        checkParameter(userJoinParam);
        userJoinVerificationService.emailVerify(userJoinParam.getUniqueId(), userJoinParam.getEmail());
        Users user = userJoinCommonService.addUser(userJoinParam);

        userJoinVerificationService.deleteEmailVerification(userJoinParam.getUniqueId(), userJoinParam.getEmail());
        return UserSimple.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .loginType(userJoinParam.getLoginType())
                .build();
    }

    private void setDummyValue(UserJoinParam userJoinParam) {
        // TODO DELETE set temp value
        userJoinParam.setOs(Os.WEB);
        userJoinParam.setVersion("0.1");
        userJoinParam.setLoginType(LoginType.EMAIL);
        // mobile could be empty
        if (userJoinParam.getMobile() == null) {
            userJoinParam.setMobile("");
        }
    }


    // 파라미터 유효성 검사
    public void checkParameter(UserJoinParam userJoinParam) {
        if (userJoinParam.getEmail() == null || !Pattern.matches(EMAIL_REG_EX, userJoinParam.getEmail())) {
            throw new InvalidParameterException(ErrorCode.EMAIL_REGEX_VALIDATION);
        }
        // 중복 Id 가입 방지
        userJoinCommonService.checkHasSameEmail(userJoinParam.getEmail());
        // 중복 전화번호 가입 방지
        userJoinCommonService.checkHasSameMobile(userJoinParam.getMobile());
    }



/*    public MemberSimple existsGoogleCreateUser(long userId, UserJoinParam userJoinParam) {
        Users user = userRepository.findByUserId(userId);
        user.setEmail(userJoinParam.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(userJoinParam.getPassword()));
        Events.raise(new JoinEvent(userId));
        return MemberSimple.builder()
                .userId(userId)
                .loginType(LoginType.IDPASS)
                .build();
    }*/

}

