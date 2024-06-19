package com.endside.social.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossIdentification {
    private String txId;
    private String appScheme;
    private String androidAppUri;
    private String iosAppUri;
    private String requestedDt;
    private String authUrl;
}
