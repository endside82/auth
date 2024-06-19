package com.endside.user.vo;

import com.endside.user.constants.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginVo {
    private long Id;
    private Long identityVerificationId;
    private Long userId;
    private String socialId;
    private ProviderType providerType;
}
