package com.endside.social.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossVerificationResultVo {
    private String resultType;
    private TossIdentificationSuccessVo success;
}
