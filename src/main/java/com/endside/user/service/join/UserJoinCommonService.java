package com.endside.user.service.join;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.JoinFailureException;
import com.endside.user.constants.UserStatus;
import com.endside.user.event.JoinEvent;
import com.endside.user.model.Agreement;
import com.endside.user.model.Device;
import com.endside.user.model.Users;
import com.endside.user.repository.AgreementRepository;
import com.endside.user.repository.DeviceRepository;
import com.endside.user.repository.UserRepository;
import com.endside.util.event.Events;
import com.endside.user.param.UserJoinParam;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserJoinCommonService {
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AgreementRepository agreementRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 디바이스 저장
    public void saveDevice(UserJoinParam userJoinParam) {
        deviceRepository.save(Device.builder()
                .userId(userJoinParam.getUserId())
                .os(userJoinParam.getOs())
                .version(userJoinParam.getVersion())
                .uniqueId(userJoinParam.getUniqueId())
                .build());
    }

    protected Users saveUser(UserJoinParam userJoinParam) {
        return userRepository.save(Users
                .builder()
                .email(userJoinParam.getEmail())
                .loginId(userJoinParam.getLoginId())
                .mobile(userJoinParam.getMobile())
                .loginType(userJoinParam.getLoginType())
                .password(bCryptPasswordEncoder.encode(userJoinParam.getPassword()))
                .status(UserStatus.NORMAL)
                .build());
    }

    protected Agreement saveAgreement(long userId, boolean isAgreeMarketing, boolean isAgreeParentAlarm) {
        return agreementRepository.save(
                Agreement.builder()
                        .userId(userId)
                        .agreeTerm(1)
                        .agreePrivacy(1)
                        .agreeMarketing(isAgreeMarketing ? 1 : 0)
                        .agreeParentAlarm(isAgreeParentAlarm ? 1 : 0)
                        .marketingModifiedAt(LocalDateTime.now())
                        .parentAlarmModifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    // 모바일 중복 검사
    public void checkHasSameMobile(String mobile) {
        userRepository.findByMobile(mobile).ifPresent(preUser -> {
            throw new JoinFailureException(ErrorCode.EXIST_PHONE_NO);
        });
    }

    // 이메일 중복 검사
    public void checkHasSameEmail(String email) {
        userRepository.findByEmail(email).ifPresent(preUser -> {
            throw new JoinFailureException(ErrorCode.EXIST_EMAIL);
        });
    }

    public Users addUser(UserJoinParam userJoinParam) {
        // Add new User to DB
        Users newUsers = saveUser(userJoinParam);
        if (newUsers == null) {
            throw new JoinFailureException(ErrorCode.FAILED_TO_JOIN_SAVE_USER);
        }
        // Set userId
        long userId = newUsers.getUserId();
        userJoinParam.setUserId(userId);
        // Add new device to DB
        saveDevice(userJoinParam);
        // Add agreement
        saveAgreement(userId, userJoinParam.isAgreeMarketing(), userJoinParam.isAgreeParentAlarm());
        // 후처리 이벤트
        Events.raise(new JoinEvent(userId));
        return newUsers;
    }
}
