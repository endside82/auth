package com.endside.social.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossIdentificationAuthVo {
    private String resultType;
    private TossIdentification success;
}
