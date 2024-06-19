package com.endside.sms.service;

import com.endside.config.db.redis.SmsAuthRedisRepository;
import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.InvalidParameterException;
import com.endside.config.error.exception.JoinFailureException;
import com.endside.config.error.exception.RestException;
import com.endside.config.util.AmazonSmsUtil;
import com.endside.email.vo.PasswordToken;
import com.endside.sms.constants.SmsType;
import com.endside.sms.model.SmsPassCode;
import com.endside.user.repository.UserRepository;
import com.endside.util.RedisKeyGen;
import com.endside.sms.model.OtpSmsCheck;
import com.endside.sms.model.OtpSmsSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SmsService {
    @Value("${sms.exceed_cnt}")
    private int SMS_EXCEEDED_CNT; // 제한 횟수
    private static final int RESEND_WAIT_TIME = 1; // 재발송 제한 시간
    private static final long PASS_CODE_EXPIRATION = 10; // 발송된 패스 코드 유효 시간 (저장 시간) 10분
    private static final int COUNT_STORE_TIME = 60 * 24; // 24h 60*24
    private static final int AUTH_VALID_TIME = 60 * 6; // 6h 60*24
    private static final int PW_CHANGE_TIME = 60; // 1h 60*24

    private final UserRepository userRepository;
    private final AmazonSmsUtil amazonSmsUtil;
    private final SmsContentBuilder smsContentBuilder;
    private final SmsAuthRedisRepository smsAuthRedisRepository;
    private final RedisKeyGen redisKeyGen;
    private final static String MOBILE_REG_EX = "(\\d+)";


    public SmsService(UserRepository userRepository, AmazonSmsUtil amazonSmsUtil, SmsContentBuilder smsContentBuilder, SmsAuthRedisRepository smsAuthRedisRepository, RedisKeyGen redisKeyGen) {
        this.userRepository = userRepository;
        this.amazonSmsUtil = amazonSmsUtil;
        this.smsContentBuilder = smsContentBuilder;
        this.smsAuthRedisRepository = smsAuthRedisRepository;
        this.redisKeyGen = redisKeyGen;
    }

    /**
     * 1.1 회원가입시 전화번호 확인 OTP SMS 발송
     *
     * @param otpSmsSend
     * @throws Exception
     */
    public void createOtpToJoinViaSms(OtpSmsSend otpSmsSend) throws Exception {
        // 전화번호 중복 검사
        checkHasSameMobile(otpSmsSend.getMobile());
        String id = otpSmsSend.getUniqueId();
        createOtpViaSms(otpSmsSend, SmsType.OTP_TO_JOIN, id);
    }

    /**
     * 2.1 패스워드 변경을 위한 SMS발송
     *
     * @param otpSmsSend
     * @throws Exception
     */
    public void createOtpToChangePwViaSms(OtpSmsSend otpSmsSend) throws Exception {
        checkValidMobileFormat(otpSmsSend.getMobile());
        long userIndex = getUserIdByIdAndMobile(otpSmsSend.getMobile());
        createOtpViaSms(otpSmsSend, SmsType.OTP_TO_PASSWORD, String.valueOf(userIndex));
    }

    /**
     * X.1 OTP 생성 및 SMS 발송
     *
     * @param otpSmsSend
     * @param smsType
     * @throws Exception
     */
    public void createOtpViaSms(OtpSmsSend otpSmsSend, SmsType smsType, String id) throws Exception {
        checkValidMobileFormat(otpSmsSend.getMobile());
        // 발송 횟수 제한을 위한 저장
        // 1.JOIN KEY = authSmsCode:CNT:J:{unique_id}:{mobile}
        // 2.PW KEY = authSmsCode:CNT:P:{user_id}:{mobile}
        String cntCacheId = redisKeyGen.generateKeyId(RedisKeyGen.COUNT, smsType.getStrCode(), id, otpSmsSend.getMobile());
        // OTP 저장을 위한 저장
        // 1.JOIN KEY = authSmsCode:OTP:J:{unique_id}:{mobile}
        // 2.PW KEY = authSmsCode:OTP:P:{user_id}:{mobile}
        String otpCacheId = redisKeyGen.generateKeyId(RedisKeyGen.OTP, smsType.getStrCode(), id, otpSmsSend.getMobile());
        // 기존 SMS 발송 횟수 체크
        SmsPassCode cntSmsPassCode = checkCntAndGetIfExist(cntCacheId);

        // 횟수만 체크할 경우
        if (otpSmsSend.isCheckOnly()) return;

        int otp = generateAuthNo6();
        // SMS 발송할 내용
        String content = smsContentBuilder.buildContentOtp(smsType, otp);
        log.info("sms send contents:" + content);
        // send sms
        amazonSmsUtil.pubTextSMS(otpSmsSend.getMobile(), content);
        // save count (SMS 발송 횟수 저장)
        addCountOrSaveNewIfNotExist(cntSmsPassCode, cntCacheId);
        // save otp
        saveOtpToCache(otpCacheId, otp);
    }

    // 모바일 중복 검사
    private void checkHasSameMobile(String mobile) {
        userRepository.findByMobile(mobile).ifPresent(preUser -> {
            throw new JoinFailureException(ErrorCode.EXIST_PHONE_NO);
        });
    }

    // 회원 일련번호 확인
    private long getUserIdByMobile(String mobile) {
        return userRepository.findByMobile(mobile).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        ).getUserId();
    }

    // 모바일로 전송한 OTP를 저장한다.
    public void saveOtpToCache(String otpCacheId, int otp) {
        SmsPassCode smsPassCode = new SmsPassCode(otpCacheId, otp, PASS_CODE_EXPIRATION);
        smsAuthRedisRepository.save(smsPassCode);
    }

    // 6자리 랜덤숫자 생성
    private int generateAuthNo6() {
        int randomNum;
        int length;
        do {
            randomNum = ThreadLocalRandom.current().nextInt(100000, 1000000);
            length = (int) (Math.log10(randomNum) + 1);
        } while (length != 6 && randomNum == 0);
        return randomNum;
    }

    // 기존에 저장한 시도횟수가 있으면 유효성 검사 하고 기존 저장 데이터를 가져온다.
    private SmsPassCode checkCntAndGetIfExist(String cntCacheId) {
        return smsAuthRedisRepository.findById(cntCacheId)
                .filter(this::checkAuthCountWithinTheDay) // 기존 저장된 count 확인
                .orElse(null);
    }

    // 횟수 체크 및 시간내 체크 확인
    private boolean checkAuthCountWithinTheDay(SmsPassCode cntSmsPassCode) {
        // 전체 발송 횟수를 넘는지 확인
        if (cntSmsPassCode.getCnt() >= SMS_EXCEEDED_CNT) {
            throw new RestException(ErrorCode.CNT_EXCEED_DAILY); // 하루 발송 횟수를 넘었는지 확인
        }
        // 저장 시각
        LocalDateTime sendLdt = LocalDateTime.ofInstant(cntSmsPassCode.getSendDate().toInstant(), ZoneId.systemDefault());
        // 대기 시간을 넘었는지 확인
        if (sendLdt.plusMinutes(RESEND_WAIT_TIME).isAfter(LocalDateTime.now())) {
            throw new RestException(ErrorCode.SEND_BEFORE_LIMIT_TIME);
        }
        return true;
    }

    // 기존에 횟수값이 저장되어 있으면 하나를 더하고 아니면 새로 만들어서 저장
    private void addCountOrSaveNewIfNotExist(SmsPassCode cntSmsPassCode, String cntCacheId) {
        if (cntSmsPassCode == null) {
            cntSmsPassCode = new SmsPassCode(cntCacheId, COUNT_STORE_TIME);
            cntSmsPassCode.setCnt(1);
        } else {
            cntSmsPassCode.setCnt(cntSmsPassCode.getCnt() + 1);
        }
        cntSmsPassCode.setSendDate(new Date());
        smsAuthRedisRepository.save(cntSmsPassCode);
    }

    // 1.2 회원가입시 전화번호 확인 OTP 인증 번호 확인
    public void checkOtpToJoinViaSms(OtpSmsCheck otpSmsCheck) {
        String uniqueId = otpSmsCheck.getUniqueId();
        checkOtp(otpSmsCheck, SmsType.OTP_TO_JOIN, uniqueId);
        saveOtpAuthUntilJoin(uniqueId, otpSmsCheck.getMobile());
    }

    // 1.2.X 인증완료된 아이디+전화번호 정보를 저장한다.
    private void saveOtpAuthUntilJoin(String id, String mobile) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_JOIN.getStrCode(), id, mobile);
        SmsPassCode authSmsPassCode = new SmsPassCode(authCacheId, AUTH_VALID_TIME);
        authSmsPassCode.setSendDate(new Date());
        smsAuthRedisRepository.save(authSmsPassCode);
    }

    // 2.2 패스워드 변경 위한 SMS 인증 번호 확인
    public PasswordToken checkOtpToChangePwViaSms(OtpSmsCheck otpSmsCheck) {
        String userId = String.valueOf(getUserIdByMobile(otpSmsCheck.getMobile()));
        checkOtp(otpSmsCheck, SmsType.OTP_TO_PASSWORD, userId);
        String token = saveOtpTokenUntilChangePw(otpSmsCheck.getMobile());
        PasswordToken passwordToken = new PasswordToken();
        passwordToken.setToken(token);
        return passwordToken;
    }

    /**
     * X.2 OTP 확인
     *
     * @param otpSmsCheck
     * @param smsType
     */
    private void checkOtp(OtpSmsCheck otpSmsCheck, SmsType smsType, String id) {
        // 1. 저장된 OTP 확인 JOIN = authSmsCode:OTP:J:{unique_id}:{mobile}
        // 2. 저장된 OTP 확인 PW = authSmsCode:OTP:P:{user_id}:{mobile}
        String otpCacheId = redisKeyGen.generateKeyId(RedisKeyGen.OTP, smsType.getStrCode(), id, otpSmsCheck.getMobile());
        SmsPassCode smsPassCode = smsAuthRedisRepository.findById(otpCacheId)
                .orElseThrow(
                () -> new RestException(ErrorCode.NON_EXIST_OTP)
        );
        // OTP 번호 매칭 확인
        if (smsPassCode.getOtp() != otpSmsCheck.getOtp()) {
            throw new RestException(ErrorCode.OTP_MISMATCH);
        }
        // 확인된 OTP 정보 삭제
        smsAuthRedisRepository.deleteById(otpCacheId);
    }

    // 인증 완료된 이메일(계정)에 대한 패스워드 변경 권한을 저장한다.
    private String saveOtpTokenUntilChangePw(String mobile) {
        String token = UUID.randomUUID().toString();
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_PASSWORD.getStrCode(), token, mobile);
        SmsPassCode authSmsPassCode = new SmsPassCode(authCacheId, PW_CHANGE_TIME);
        authSmsPassCode.setSendDate(new Date());
        smsAuthRedisRepository.save(authSmsPassCode);
        return token;
    }

    // 인증완료된 아이디+ 바꿀 핸드폰 정보를 저장한다.
    private void saveOtpAuthUntilChangeMobile(String id, String toSms) {
        String authCacheId = redisKeyGen.generateKeyId(RedisKeyGen.AUTH, SmsType.OTP_TO_MOBILE.getStrCode(), id, toSms);
        SmsPassCode authSmsPassCode = new SmsPassCode(authCacheId, AUTH_VALID_TIME);
        authSmsPassCode.setSendDate(new Date());
        smsAuthRedisRepository.save(authSmsPassCode);
    }

    private long getUserIdByIdAndMobile(String mobile) {
        return userRepository.findByMobile(mobile).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        ).getUserId();
    }

    // 파라미터 유효성 검사
    public void checkValidMobileFormat(String mobile) {
        if (!Pattern.matches(MOBILE_REG_EX, mobile)) {
            throw new InvalidParameterException(ErrorCode.MOBILE_REGEX_VALIDATION);
        }
    }

}
