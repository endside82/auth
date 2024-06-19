package com.endside.social.service;

import com.endside.social.constants.TossConstants;
import com.endside.social.vo.TossAccessTokenVo;
import com.endside.social.vo.TossIdentificationVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static junit.framework.TestCase.assertEquals;

/**
 * 토스 본인 인증(간편인증)
 */
public class TossVerificationServiceTest {
    public static MockWebServer mockBackEnd;
    private TossVerificationService tossVerificationService;
    private TossVerificationService mockTossVerificationService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }


    @Test
    public void set_access_token_then_setup_access_token() throws Exception {
        mockTossVerificationService = new TossVerificationService();
        ReflectionTestUtils.setField(TossVerificationService.class, "TOSS_BEARER_KEY", "aaa");
        ReflectionTestUtils.setField(TossVerificationService.class, "TOSS_BEARER_KEY_EXPIRE_DATETIME", LocalDateTime.now().plusSeconds(3600));
        TossIdentificationVo tossIdentificationVo = mockTossVerificationService.getTossAuthUrlAndTxId();

    }
    @Test
    public void set_access_token_then_setup_access_token_mock() throws Exception {
        String tossMockUrl = mockBackEnd.url("").toString();
        mockTossVerificationService = new TossVerificationService(tossMockUrl);

        long timeInSeconds = LocalDateTime.now()
                .plusMinutes(10)
                .toEpochSecond(ZoneOffset.of("+09:00"));
        String token = "abcdefghijlmnop";
        TossAccessTokenVo tossAccessTokenVo = TossAccessTokenVo.builder()
                .accessToken(token)
                .scope(TossConstants.CA)
                .tokenType("Bearer")
                .expiresIn(timeInSeconds).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String body;
        try {
            body = objectMapper.writeValueAsString(tossAccessTokenVo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        mockBackEnd.enqueue(new MockResponse()
                .setBody(body)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        Mono<TossAccessTokenVo> tossAccessTokenVoMono = tossVerificationService.getAccessToken();
        StepVerifier.create(tossAccessTokenVoMono)
                .expectNextMatches(retrieveVo -> retrieveVo.getAccessToken()
                        .equals(token))
                .verifyComplete();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("POST", recordedRequest.getMethod());
        Assertions.assertEquals(ReflectionTestUtils.getField(TossVerificationService.class , "TOSS_AUTH_PATH"), recordedRequest.getPath());
    }




}

