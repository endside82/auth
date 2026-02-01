package com.endside.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.constants.ProviderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserSimpleVo {

    private long userId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String loginId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mobile;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LoginType loginType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ProviderType providerType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime initAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Os os;
}
