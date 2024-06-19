package com.endside.social.vo;

import com.endside.user.constants.ProviderType;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SocialProviderVerifyVo implements Serializable {
    private String email;
    private String providerId;
    private ProviderType providerType;
}
