package com.endside.user.service;

import com.endside.config.db.redis.LoginAttemptRepository;
import com.endside.config.redis.LoginAttempt;
import org.springframework.stereotype.Service;

import static org.springframework.data.util.Optionals.ifPresentOrElse;

/**
 * 로그인 실패 시도 기록
 */
@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 10; // 로그인 시도 최대 횟수 : 10번
    private final int WAITING_TIME = 600; // 로그인 시도 초과 이후 접근을 거부 하는 시간 : 600초 = 10분
    private final int ELIMINATE_TIME = 1; // 1초후 즉시 만료 시킴

    // 로그인 시도 기록
    private final LoginAttemptRepository loginAttemptRepository;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    // 로그인 성공시
    public void loginSucceeded(String key) {
        loginAttemptRepository.findById(key).ifPresent(
            found -> {
                found.setTimeToLive(ELIMINATE_TIME);
                loginAttemptRepository.save(found);
            }
        );
    }

    // 로그인 실패시
    public void loginFailed(String key) {
        ifPresentOrElse(
            loginAttemptRepository.findById(key),
            found -> {
                int cnt = found.getCnt();
                cnt++;
                if(cnt == MAX_ATTEMPT) {
                    found.setTimeToLive(WAITING_TIME);
                }
                found.setCnt(cnt);
                loginAttemptRepository.save(found);
            }, () -> loginAttemptRepository.save(new LoginAttempt(key))
        );
    }

    // 블럭 상태인지 확인
    public boolean isBlocked(String key) {
        return loginAttemptRepository.findById(key)
                .map(found -> found.getCnt() >= MAX_ATTEMPT)
                .orElse(false);
    }

    // 로그인 시도 횟수 가져오기
    public int getCount(String key) {
        return loginAttemptRepository.findById(key)
                .map(LoginAttempt::getCnt)
                .orElse(0);
    }

}
