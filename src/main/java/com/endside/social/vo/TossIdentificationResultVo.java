package com.endside.social.vo;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TossIdentificationResultVo {
    private String txId;
    private String ci;
    private String name;
    private String birthday;
    private String gender;
    private String phone;
}
