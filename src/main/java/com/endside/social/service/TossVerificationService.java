package com.endside.social.service;

import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.RestException;
import com.endside.config.error.exception.SocialVerificationException;
import com.endside.social.constants.TossConstants;
import com.endside.social.exception.TossAccessTokenException;
import com.endside.social.exception.TossServerException;
import com.endside.social.param.TossAuthResultParam;
import com.endside.social.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.toss.cert.sdk.TossCertSession;
import im.toss.cert.sdk.TossCertSessionGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

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
    private final TossCertSessionGenerator tossCertSessionGenerator;
    private final WebClient authWebClient;
    private final WebClient certWebClient;


    public TossVerificationService() {
        this.authWebClient = WebClient.builder()
                .baseUrl(TOSS_AUTH_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        this.certWebClient = WebClient.builder()
                .baseUrl(TOSS_CERT_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.tossCertSessionGenerator = new TossCertSessionGenerator();
    }

    protected TossVerificationService(String tossUrl) {
        this.authWebClient = WebClient.builder()
                .baseUrl(tossUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        this.certWebClient = WebClient.builder()
                .baseUrl(tossUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.tossCertSessionGenerator = new TossCertSessionGenerator();
    }


    /**
     * Access Token 발급 API 호출
     */

    public void setAccessToken() {
        if (UPDATED_TIME != null && UPDATED_TIME.isAfter(LocalDateTime.now().minusSeconds(10))) {
            log.debug("It's just updated!");
            return;
        }
        LOCK.lock();
        try {
            Optional<TossAccessTokenVo> tossAccessTokenVoOpt = Optional.ofNullable(getAccessToken().block());
            tossAccessTokenVoOpt.orElseThrow(() -> new RestException(ErrorCode.FAILED_GET_TOSS_ACCESS_TOKEN));
            TossAccessTokenVo tossAccessTokenVo = tossAccessTokenVoOpt.get();
            TOSS_BEARER_KEY = tossAccessTokenVo.getAccessToken();
            TOSS_BEARER_KEY_EXPIRE_DATETIME = LocalDateTime.now().plusSeconds(tossAccessTokenVo.getExpiresIn());
            UPDATED_TIME = LocalDateTime.now();
        } finally {
            LOCK.unlock();
        }
    }

    public Mono<TossAccessTokenVo> getAccessToken() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(TossConstants.GRANT_TYPE, TossConstants.CLIENT_CREDENTIALS);
        requestBody.add(TossConstants.CLIENT_ID, "test_a8e23336d673ca70922b485fe806eb2d");
        requestBody.add(TossConstants.CLIENT_SECRET, "test_418087247d66da09fda1964dc4734e453c7cf66a7a9e3");
        requestBody.add(TossConstants.SCOPE, TossConstants.CA);

        return authWebClient
                .post()
                .uri(TOSS_AUTH_PATH)
                .body(BodyInserters.fromFormData(requestBody))
                .retrieve()
                .bodyToMono(TossAccessTokenVo.class);
    }

    public TossIdentificationVo getTossAuthUrlAndTxId() {
        TossIdentification tossIdentification = getIdentificationAuthUrl().getSuccess();
        return TossIdentificationVo.builder()
                .authUrl(tossIdentification.getAuthUrl())
                .txId(tossIdentification.getTxId())
                .build();
    }

    private TossIdentificationAuthVo getIdentificationAuthUrl() {
        if (TOSS_BEARER_KEY == null
                || TOSS_BEARER_KEY_EXPIRE_DATETIME == null
                || TOSS_BEARER_KEY_EXPIRE_DATETIME.isBefore(LocalDateTime.now())) {
            setAccessToken();
        }
        Mono<TossIdentificationAuthVo> tossIdentificationAuthVoMono =
            certWebClient
                .post()
                .uri(TOSS_CERT_ID_REQUEST_PATH)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(TOSS_BEARER_KEY))
                .body(BodyInserters.fromValue("{\"requestType\": \"USER_NONE\"}"))
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new TossServerException()))
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new TossAccessTokenException()))
                .bodyToMono(TossIdentificationAuthVo.class)
                // .onErrorResume(err -> setAccessToken().flatMap(nothing -> getTossAuthUrl()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof TossServerException) // TossServerException 일때만 retry
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new TossServerException(); // retry를 다 마치고
                        })
                        .jitter(0.7));
        TossIdentificationAuthVo tossIdentificationAuthVo;
        try {
            tossIdentificationAuthVo = tossIdentificationAuthVoMono.block();
        } catch (TossAccessTokenException e) {
            TOSS_BEARER_KEY = null;
            tossIdentificationAuthVo = tossIdentificationAuthVoMono.block();
        }
        if (tossIdentificationAuthVo == null) {
            throw new SocialVerificationException(ErrorCode.SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL);
        }
        return tossIdentificationAuthVo;
    }


    public TossIdentificationResultVo verifyIdentification(String txId) {
        if (TOSS_BEARER_KEY == null
                || TOSS_BEARER_KEY_EXPIRE_DATETIME == null
                || TOSS_BEARER_KEY_EXPIRE_DATETIME.isBefore(LocalDateTime.now())) {
            setAccessToken();
        }
        TossCertSession tossCertSession = tossCertSessionGenerator.generate();

        String bodyStr = makeRequestBodyString(txId, tossCertSession.getSessionKey());
        Mono<TossVerificationResultVo> tossVerificationResultVoMono = certWebClient.post()
                .uri(TOSS_CERT_ID_RESULT_PATH)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(TOSS_BEARER_KEY))
                .body(BodyInserters.fromValue(bodyStr))
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new TossServerException()))
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new TossAccessTokenException()))
                .bodyToMono(TossVerificationResultVo.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof TossServerException) // TossServerException 일때만 retry
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new TossServerException(); // retry를 다 끝나고 보낼것
                        })
                        .jitter(0.7));

        TossVerificationResultVo tossVerificationResultVo;

        try {
            tossVerificationResultVo = tossVerificationResultVoMono.block();
        } catch (TossAccessTokenException e) {
            TOSS_BEARER_KEY = null;
            tossVerificationResultVo = tossVerificationResultVoMono.block();
        }
        if (tossVerificationResultVo == null) {
            throw new SocialVerificationException(ErrorCode.SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL);
        }
        TossPersonalData personalData = tossVerificationResultVo.getSuccess().getPersonalData();
        return TossIdentificationResultVo.builder()
                .txId(txId)
                .ci(tossCertSession.decrypt(personalData.getCi()))
                .gender(tossCertSession.decrypt(personalData.getGender()))
                .phone(tossCertSession.decrypt(personalData.getPhone()))
                .name(tossCertSession.decrypt(personalData.getName()))
                .birthday(tossCertSession.decrypt(personalData.getBirthday()))
                .build();
    }

    private String makeRequestBodyString(String txId, String sessionKey) {
        TossAuthResultParam request = new TossAuthResultParam(txId, sessionKey);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
