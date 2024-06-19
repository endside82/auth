package com.endside.social.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.endside.config.error.exception.SocialAuthenticationException;
import com.endside.social.vo.SocialUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Service
public class GoogleVerificationService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleVerificationService() {
        this.verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList("864687491322-od9qc37qtppdqfm78pqm109sh8b4jm15.apps.googleusercontent.com"))
                .build();
    }

    public SocialUserInfo getProviderInfo(String credential) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(credential);
        } catch (GeneralSecurityException | IOException e) {
            throw new SocialAuthenticationException();
        }
        if (idToken == null) {
            throw new SocialAuthenticationException();
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String socialId = payload.getSubject();
        String email = payload.getEmail();
        // Get profile information from payload
        /*
        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String locale = (String) payload.get("locale");
        String familyName = (String) payload.get("family_name");
        String givenName = (String) payload.get("given_name");*/
        return SocialUserInfo.builder()
                .email(email)
                .socialUniqueId(socialId)
                .build();
    }


}
