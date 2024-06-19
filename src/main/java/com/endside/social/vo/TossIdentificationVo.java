package com.endside.social.vo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TossIdentificationVo {
    private String txId;
    private String authUrl;
}
