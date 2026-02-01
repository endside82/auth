package com.endside.social.service;

import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.RestException;
import com.endside.social.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Toss 본인인증 서비스
 *
 * 주의: 이 서비스를 사용하려면 Toss 인증 SDK가 필요합니다.
 * 1. build.gradle에 Toss SDK 의존성 추가
 * 2. application.yml에 toss.client-id, toss.client-secret 설정
 * 3. toss.enabled=true 설정
 *
 * Toss SDK 없이는 기본적으로 비활성화됩니다.
 */
@Service
@Slf4j
public class TossVerificationService {

    private static final String TOSS_AUTH_URL = "https://oauth2.cert.toss.im";
    private static final String TOSS_CERT_URL = "https://cert.toss.im";
    private static final String TOSS_AUTH_PATH = "/token";
    private static final String TOSS_CERT_ID_REQUEST_PATH = "/api/v2/sign/user/auth/request";
    private static final String TOSS_CERT_ID_RESULT_PATH = "/api/v2/sign/user/auth/result";

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static String TOSS_BEARER_KEY = null;
    private static LocalDateTime TOSS_BEARER_KEY_EXPIRE_DATETIME = null;
    private static LocalDateTime UPDATED_TIME = null;

    private final WebClient authWebClient;
    private final WebClient certWebClient;
    private final String tossClientId;
    private final String tossClientSecret;
    private final boolean tossEnabled;

    public TossVerificationService(
            @Value("${toss.client-id:}") String tossClientId,
            @Value("${toss.client-secret:}") String tossClientSecret,
            @Value("${toss.enabled:false}") boolean tossEnabled) {
        this.tossClientId = tossClientId;
        this.tossClientSecret = tossClientSecret;
        this.tossEnabled = tossEnabled;
        this.authWebClient = WebClient.builder()
                .baseUrl(TOSS_AUTH_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        this.certWebClient = WebClient.builder()
                .baseUrl(TOSS_CERT_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Toss 인증 URL 및 트랜잭션 ID 조회
     * Toss SDK가 없으면 예외 발생
     */
    public TossIdentificationVo getTossAuthUrlAndTxId() {
        if (!tossEnabled) {
            log.warn("Toss verification is disabled. Set toss.enabled=true and configure Toss SDK.");
            throw new RestException(ErrorCode.SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL);
        }

        // Toss SDK 사용 시 아래 코드 활성화 필요
        // TossCertSession tossCertSession = tossCertSessionGenerator.generate();
        // ... 실제 Toss 연동 로직

        throw new RestException(ErrorCode.SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL);
    }

    /**
     * 본인인증 검증
     */
    public TossIdentificationResultVo verifyIdentification(String txId) {
        if (!tossEnabled) {
            log.warn("Toss verification is disabled.");
            throw new RestException(ErrorCode.SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL);
        }

        throw new RestException(ErrorCode.SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL);
    }

    /**
     * Access Token 발급
     */
    public void setAccessToken() {
        if (!tossEnabled || tossClientId.isEmpty() || tossClientSecret.isEmpty()) {
            log.warn("Toss credentials not configured.");
            return;
        }

        if (UPDATED_TIME != null && UPDATED_TIME.isAfter(LocalDateTime.now().minusSeconds(10))) {
            log.debug("Token was just updated!");
            return;
        }

        LOCK.lock();
        try {
            TossAccessTokenVo tossAccessTokenVo = getAccessToken().block();
            if (tossAccessTokenVo == null) {
                throw new RestException(ErrorCode.FAILED_GET_TOSS_ACCESS_TOKEN);
            }
            TOSS_BEARER_KEY = tossAccessTokenVo.getAccessToken();
            TOSS_BEARER_KEY_EXPIRE_DATETIME = LocalDateTime.now().plusSeconds(tossAccessTokenVo.getExpiresIn());
            UPDATED_TIME = LocalDateTime.now();
        } finally {
            LOCK.unlock();
        }
    }

    private Mono<TossAccessTokenVo> getAccessToken() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("client_id", tossClientId);
        requestBody.add("client_secret", tossClientSecret);
        requestBody.add("scope", "ca");

        return authWebClient
                .post()
                .uri(TOSS_AUTH_PATH)
                .body(BodyInserters.fromFormData(requestBody))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Failed to get Toss access token: {}", response.statusCode());
                    return Mono.error(new RestException(ErrorCode.FAILED_GET_TOSS_ACCESS_TOKEN));
                })
                .bodyToMono(TossAccessTokenVo.class);
    }
}
