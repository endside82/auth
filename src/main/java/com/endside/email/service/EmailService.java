package com.endside.email.service;

import com.endside.config.db.redis.EmailAuthRedisRepository;
import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.EmailRestException;
import com.endside.config.error.exception.InvalidParameterException;
import com.endside.config.error.exception.JoinFailureException;
import com.endside.email.constants.EmailType;
import com.endside.util.RedisKeyGen;
import com.endside.email.model.EmailPassCode;
import com.endside.email.model.OtpMailCheck;
import com.endside.email.model.OtpMailSend;
import com.endside.user.repository.UserRepository;
import com.endside.email.vo.PasswordToken;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
public class EmailService {
    @Value("${email.exceed_cnt}")
    private int EMAIL_EXCEEDED_CNT; // 제한 횟수

    private static final int RESEND_WAIT_TIME = 1; // 재발송 제한 시간 1분
    private static final long PASS_CODE_EXPIRATION = 10; // 발송된 패스 코드 유효 시간 (저장 시간) 10분
    private static final int COUNT_STORE_TIME = 60 * 24; // 24h 60*24
    private static final int AUTH_VALID_TIME = 60 * 6; // 6h 60*24
    private static final int PW_CHANGE_TIME = 60; // 1h 60

    private final UserRepository userRepository;
    private final AmazonSESService amazonSESService;
    private final MailContentBuilder mailContentBuilder;
    private final EmailAuthRedisRepository emailAuthRedisRepository;

    private final RedisKeyGen generateKeyId;
    private final static String EMAIL_REG_EX = "^(.+)@(.+)$";

    public EmailService(UserRepository userRepository, AmazonSESService amazonSESService, MailContentBuilder mailContentBuilder, EmailAuthRedisRepository emailAuthRedisRepository, RedisKeyGen generateKeyId) {
        this.userRepository = userRepository;
        this.amazonSESService = amazonSESService;
        this.mailContentBuilder = mailContentBuilder;
        this.emailAuthRedisRepository = emailAuthRedisRepository;
        this.generateKeyId = generateKeyId;
    }

    /**
     * 1.1 회원가입시 이메일 확인 OTP 이메일 발송
     */
    public void createOtpToJoinViaEmail(OtpMailSend otpMailSend) throws Exception {
        // 이메일 포맷 검사
        checkValidEmailFormat(otpMailSend.getEmail());
        // 이메일 중복 검사
        checkHasSameEmail(otpMailSend.getEmail());
        createOtpViaEmail(otpMailSend, EmailType.OTP_TO_JOIN, otpMailSend.getUniqueId());
    }

    /**
     * 2.2 패스워드 변경을 위한 OTP 메일 폼 발송
     *
     * @throws Exception
     */
    public void createOtpToChangePwViaEmail(OtpMailSend otpMailSend) throws Exception {
        // 이메일 포맷 검사
        checkValidEmailFormat(otpMailSend.getEmail());
        long userIndex = getUserIdByEmail(otpMailSend.getEmail());
        createOtpViaEmail(otpMailSend, EmailType.OTP_TO_PASSWORD, String.valueOf(userIndex));
    }

    // TODO DELETE TEMP
    private String getIdString(String id, String email) {
        if (!StringUtils.hasText(id) && StringUtils.hasText(email)) {
            return email.substring(0, email.indexOf("@"));
        }
        return id;
    }

    // 이메일 변경 OTP 이메일 발송

    /**
     * 3.1 이메일 변경을 위한 메일 폼 발송
     */
    public void createOtpToChangeEmailViaEmail(OtpMailSend otpMailSend, long userIndex) throws Exception {
        String inputEmail = otpMailSend.getEmail();
        checkValidEmailFormat(inputEmail);
        checkHasSameEmail(inputEmail);
        String email = getEmailByUserId(userIndex);
        if (inputEmail.equals(email)) {
            throw new EmailRestException(ErrorCode.SAME_CURRENT_EMAIL);
        }
        createOtpViaEmail(otpMailSend, EmailType.OTP_TO_EMAIL, String.valueOf(userIndex));
    }

    /**
     * X.1 OTP 생성 및 이메일 발송
     *
     * @param otpMailSend
     * @param emailType
     * @param identifier
     * @throws Exception
     */
    public void createOtpViaEmail(OtpMailSend otpMailSend, EmailType emailType, String identifier) throws Exception {
        // 받을 대상 이메일
        String toEmail = otpMailSend.getEmail();
        // 발송 횟수 제한을 위한 저장 KEY
        // authEmailCode:CNT:P:{user_id}:{email}
        String cntCacheId = generateKeyId.generateKeyId(RedisKeyGen.COUNT, emailType.getStrCode(), identifier, toEmail);
        // OTP 저장을 위한 저장 KEY
        // authEmailCode:OTP:P:{user_id}:{email}
        String otpCacheId = generateKeyId.generateKeyId(RedisKeyGen.OTP, emailType.getStrCode(), identifier, toEmail);
        // 기존 Email 발송 횟수 정보
        EmailPassCode cntEmailPassCode = checkCntAndGetIfExist(cntCacheId);

        // 횟수만 체크할 경우
        if (otpMailSend.isCheckOnly()) return;

        int otp = generateAuthNo6();
        // 발송할 내용
        String subject = mailContentBuilder.getSubject(emailType);              // 제목
        String content = mailContentBuilder.buildContentOtp(emailType, otp);   // 내용

        // send email
        amazonSESService.send(toEmail, subject, content);
        // save count (email 발송 횟수 저장)
        addCountOrSaveNewIfNotExist(cntEmailPassCode, cntCacheId);
        // save otp
        saveOtpToCache(otpCacheId, otp);
    }

    // 이메일 중복 검사
    private void checkHasSameEmail(String email) {
        userRepository.findByEmail(email).ifPresent(preUser -> {
            throw new JoinFailureException(ErrorCode.EXIST_EMAIL);
        });
    }

    // 이메일로 발생한 OTP를 저장한다.
    private void saveOtpToCache(String otpCacheId, int otp) {
        EmailPassCode emailPassCode = new EmailPassCode(otpCacheId, otp, PASS_CODE_EXPIRATION);
        emailAuthRedisRepository.save(emailPassCode);
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
    private EmailPassCode checkCntAndGetIfExist(String cntCacheId) {
        // 기존에 생성된 count 가 있는지
        return emailAuthRedisRepository.findById(cntCacheId)
                .filter(this::checkAuthCountWithinTheDay)
                .orElse(null);
    }

    // 횟수 체크 및 시간내 체크 확인
    private boolean checkAuthCountWithinTheDay(EmailPassCode cntEmailPassCode) {
        // 전체 발송 횟수를 넘는지 확인
        if (cntEmailPassCode.getCnt() >= EMAIL_EXCEEDED_CNT) {
            throw new EmailRestException(ErrorCode.CNT_EXCEED_DAILY);
        }
        // 저장 시각
        LocalDateTime sendLdt = LocalDateTime.ofInstant(cntEmailPassCode.getSendDate().toInstant(), ZoneId.systemDefault());
        // 대기 시간을 넘었는지 확인
        if (sendLdt.plusMinutes(RESEND_WAIT_TIME).isAfter(LocalDateTime.now())) {
            throw new EmailRestException(ErrorCode.SEND_BEFORE_LIMIT_TIME);
        }
        return true;
    }

    // 기존에 횟수값이 저장되어 있으면 하나를 더하고 아니면 새로 만들어서 저장
    private void addCountOrSaveNewIfNotExist(EmailPassCode cntEmailPassCode, String cntCacheId) {
        if (cntEmailPassCode == null) {
            cntEmailPassCode = new EmailPassCode(cntCacheId, COUNT_STORE_TIME);
            cntEmailPassCode.setCnt(1);
        } else {
            cntEmailPassCode.setCnt(cntEmailPassCode.getCnt() + 1);
        }
        cntEmailPassCode.setSendDate(new Date());
        emailAuthRedisRepository.save(cntEmailPassCode);
    }

    /**
     * 1.2 회원가입시 이메일 확인 OTP 인증 번호 확인
     *
     * @param otpMailCheck
     */
    public void checkOtpToJoinViaEmail(OtpMailCheck otpMailCheck) {
        String email = otpMailCheck.getEmail();   // 이메일
        // 이메일 포맷 검사
        checkValidEmailFormat(email);
        checkOtp(email, otpMailCheck.getUniqueId(), otpMailCheck.getOtp(), EmailType.OTP_TO_JOIN);
        saveOtpAuthUntilJoin(otpMailCheck.getUniqueId(), otpMailCheck.getEmail());
    }

    /**
     * 2.2 패스워드 변경 위한 인증 번호 확인
     *
     * @param otpMailCheck
     * @return
     */
    public PasswordToken checkOtpToChangePwViaEmail(OtpMailCheck otpMailCheck) {
        String email = otpMailCheck.getEmail();
        // 이메일 포맷 검사
        checkValidEmailFormat(email);
        String userId = String.valueOf(getUserIdByEmail(email));
        checkOtp(email, userId, otpMailCheck.getOtp(), EmailType.OTP_TO_PASSWORD);
        String token = saveOtpTokenUntilChangePw(email);
        PasswordToken passwordToken = new PasswordToken();
        passwordToken.setToken(token);
        return passwordToken;
    }

    /**
     * 3.2 이메일 변경 위한 인증 번호 확인
     *
     * @param otpMailCheck
     * @param userIndex
     */
    public void checkOtpToChangeEmailViaEmail(OtpMailCheck otpMailCheck, long userIndex) {
        // 이메일 포맷 검사
        checkValidEmailFormat(otpMailCheck.getEmail());
        checkOtp(otpMailCheck.getEmail(), String.valueOf(userIndex), otpMailCheck.getOtp(), EmailType.OTP_TO_EMAIL);
        changeEmailByUserId(otpMailCheck.getEmail(), userIndex);
    }

    public void changeEmailByUserId(String email, long userIndex) {
        userRepository.findByUserId(userIndex).ifPresent(preUser -> {
            preUser.setEmail(email);
            userRepository.save(preUser);
        });
    }

    /**
     * X.2 OTP 확인
     */
    private void checkOtp(String email, String identity, int otp, EmailType emailType) {
        // 저장된 OTP 확인
        String otpCacheId = generateKeyId.generateKeyId(RedisKeyGen.OTP, emailType.getStrCode(), identity, email);
        EmailPassCode emailPassCode =
                emailAuthRedisRepository.findById(otpCacheId)
                        .orElseThrow(
                        () -> new EmailRestException(ErrorCode.NON_EXIST_OTP)
                );

        // OTP 번호 매칭 확인
        if (emailPassCode.getOtp() != otp) {
            throw new EmailRestException(ErrorCode.OTP_MISMATCH);
        }
        // 확인된 OTP 정보 삭제
        emailAuthRedisRepository.deleteById(otpCacheId);
    }

    // 인증완료된 아이디+이메일 정보를 저장한다.
    private void saveOtpAuthUntilJoin(String id, String toEmail) {
        String authCacheId = generateKeyId.generateKeyId(RedisKeyGen.AUTH, EmailType.OTP_TO_JOIN.getStrCode(), id, toEmail);
        EmailPassCode authEmailPassCode = new EmailPassCode(authCacheId, AUTH_VALID_TIME);
        authEmailPassCode.setSendDate(new Date());
        emailAuthRedisRepository.save(authEmailPassCode);
    }

    // 인증 완료된 이메일(계정)에 대한 패스워드 변경 권한을 저장한다.
    private String saveOtpTokenUntilChangePw(String email) {
        String token = UUID.randomUUID().toString();
        String authCacheId = generateKeyId.generateKeyId(RedisKeyGen.AUTH, EmailType.OTP_TO_PASSWORD.getStrCode(), token, email);
        EmailPassCode authEmailPassCode = new EmailPassCode(authCacheId, PW_CHANGE_TIME);
        authEmailPassCode.setSendDate(new Date());
        emailAuthRedisRepository.save(authEmailPassCode);
        return token;
    }

    // 인증완료된 아이디 바꿀 이메일 정보를 저장한다.
    /*private void saveOtpAuthUntilChangeEmail(String id, String toEmail){
        String authCacheId = generateKeyId(AUTH, EmailType.OTP_TO_EMAIL, id, toEmail);
        EmailPassCode authEmailPassCode = new EmailPassCode(authCacheId, AUTH_VALID_TIME);
        authEmailPassCode.setSendDate(new Date());
        emailAuthRedisRepository.save(authEmailPassCode);
    }*/


    // 회원 일련번호 확인
    private long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EmailRestException(ErrorCode.EMAIL_NOT_FOUND)
        ).getUserId();
    }

    // 회원 이메일 확인
    private String getEmailByUserId(long userId) {
        return userRepository.findByUserId(userId).orElseThrow(
                () -> new EmailRestException(ErrorCode.EMAIL_NOT_FOUND)
        ).getEmail();
    }

    // 가입 완료 이메일 발송
    /* @Async
    public void sendJoinSuccessEmail(String email) {
        String subject = mailContentBuilder.getSubject(EmailType.JOIN_SUCCESS); // 제목
        String content = mailContentBuilder.buildContent( EmailType.JOIN_SUCCESS, email); // 내용
        // send email
        amazonSESService.send(email, subject, content);
    }*/

    public void checkValidEmailFormat(String email) {
        if (StringUtil.isNullOrEmpty(email) || !Pattern.matches(EMAIL_REG_EX, email)) {
            throw new InvalidParameterException(ErrorCode.EMAIL_REGEX_VALIDATION);
        }
    }

}
