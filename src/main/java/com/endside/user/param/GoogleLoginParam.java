package com.endside.user.param;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GoogleLoginParam {
    String clientId;
    String clientSecret;
    String code;
    String redirectUrl;
    String grantType;
}
