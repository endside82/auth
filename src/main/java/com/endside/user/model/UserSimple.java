package com.endside.user.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.ProviderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSimple {

    private long userId;

    private String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LoginType loginType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ProviderType providerType;
}



