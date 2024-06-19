package com.endside.social.vo;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TossIdentificationSuccessVo {
    private String txId;
    private String status;
    private String userIdentifier;
    private String userCiToken;
    private String signature;
    private String completedDt;
    private String requestedDt;
    private TossPersonalData personalData;

}
