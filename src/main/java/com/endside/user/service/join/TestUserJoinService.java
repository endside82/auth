package com.endside.user.service.join;


import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.model.UserSimple;
import com.endside.user.model.Users;
import com.endside.user.param.UserJoinParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class TestUserJoinService implements UserJoinService {

    private final UserJoinCommonService userJoinCommonService;

    public TestUserJoinService(UserJoinCommonService userJoinCommonService) {
        this.userJoinCommonService = userJoinCommonService;
    }


    @Override
    @Transactional(rollbackFor = {Exception.class})
    public UserSimple joinUser(UserJoinParam userJoinParam) {
        setDummyValue(userJoinParam);
        checkParameter(userJoinParam);
        Users user = userJoinCommonService.addUser(userJoinParam);
        return UserSimple.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .loginType(userJoinParam.getLoginType())
                .build();
    }

    private void setDummyValue (UserJoinParam userJoinParam) {
        String uuid = UUID.randomUUID().toString();
        String mobile = "012" + makeRandomFourDigitValue() + makeRandomFourDigitValue();

        userJoinParam.setMobile(mobile);
        // email could be empty
        if (userJoinParam.getEmail() == null) {
            userJoinParam.setEmail("");
        }
        userJoinParam.setUniqueId(uuid);
        userJoinParam.setBirthDate(LocalDate.now());
        userJoinParam.setOs(Os.WEB); // temp value
        userJoinParam.setVersion("0.1");
        userJoinParam.setLoginType(LoginType.EMAIL);
    }

    // 파라미터 유효성 검사
    private void checkParameter(UserJoinParam userJoinParam) {
        // 중복 Id 가입 방지
        userJoinCommonService.checkHasSameEmail(userJoinParam.getEmail());
        // 중복 전화번호 가입 방지
        userJoinCommonService.checkHasSameMobile(userJoinParam.getMobile());
    }

    private int makeRandomFourDigitValue() {
        return (int) (Math.random() * (9999 - 1000 + 1) + 1000);
    }

}
