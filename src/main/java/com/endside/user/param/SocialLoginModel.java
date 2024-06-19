package com.endside.user.param;

import com.endside.user.constants.Os;
import com.endside.user.constants.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginModel {
    private ProviderType providerType; // GOOGLE, KAKAO, NAVER
    private String idToken;            // GOOGLE = credential
    private Os os;
}
