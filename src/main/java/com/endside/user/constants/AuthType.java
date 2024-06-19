package com.endside.user.constants;


import lombok.Getter;

@Getter
public enum AuthType {
    MOBILE(1), EMAIL(2);

    AuthType(int typeNum) {
        this.typeNum = typeNum;
    }

    private final int typeNum;

}
