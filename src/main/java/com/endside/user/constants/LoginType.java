package com.endside.user.constants;

import lombok.Getter;

@Getter
public enum LoginType {
    EMAIL(0, "EMAIL" ),
    MOBILE(1, "MOBILE"),
    SOCIAL(2,"SOCIAL"),
    ID_PASS(3,"ID_PASS"),
    GUEST(4, "GUEST");

    private final String loginType;
    private final int typeNum;

    LoginType(int typeNum, String loginType) {
        this.typeNum = typeNum;
        this.loginType = loginType.toUpperCase();
    }

    @Override
    public String toString() {
        return this.loginType;
    }

    public static LoginType getLoginTypeAsType(String loginType) {
        return LoginType.valueOf(loginType.toUpperCase());
    }
}
