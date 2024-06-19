package com.endside.user.model;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.constants.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginAddInfo {
    private LoginType loginType;
    private ProviderType providerType;
    private Os os;
}
