package com.endside.user.service.join;

import com.endside.user.constants.Os;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.endside.user.constants.LoginType;
import com.endside.user.service.UserLeaveService;
import com.endside.user.vo.UserSimpleVo;
import com.endside.user.model.Users;
import com.endside.user.param.UserJoinParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class GuestUserJoinService implements UserJoinService {
    private final UserJoinCommonService userJoinCommonService;
    private final JoinHelperService joinHelperService;
    private final UserLeaveService userLeaveService;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public UserSimpleVo joinUser(UserJoinParam userJoinParam) {
        // 더미 값 세팅
        setDummyValue(userJoinParam);
        // 기존에 동일한 기기 고유 번호 이면서 guest 유저인 경우 탈퇴 시킨다.
        userLeaveService.leaveUserIfGuestUserHasUniqueIdExist(userJoinParam.getUniqueId());
        // 유저 가입 진행
        Users user = userJoinCommonService.addUser(userJoinParam);
        // 가입 데이터 반환
        return UserSimpleVo.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .password(userJoinParam.getPassword())
                .loginType(userJoinParam.getLoginType())
                .build();
    }

    // 더미값 세팅
    private void setDummyValue(UserJoinParam userJoinParam) {
        // 임의의 로그인 아이디 생성
        userJoinParam.setLoginId(joinHelperService.generateAlphanumericRandomString(20));
        // 임의의 패스워드 생성
        userJoinParam.setPassword(joinHelperService.generateAlphanumericRandomString(20));
        userJoinParam.setMobile("");
        userJoinParam.setEmail("");
        userJoinParam.setUniqueId(userJoinParam.getUniqueId());
        userJoinParam.setBirthDate(null);
        if(userJoinParam.getOs() == null) {
            userJoinParam.setOs(Os.IOS);
        }
        userJoinParam.setVersion("0.1");
        userJoinParam.setLoginType(LoginType.GUEST);
    }
}
