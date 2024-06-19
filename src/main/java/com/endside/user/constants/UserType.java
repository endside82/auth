package com.endside.user.constants;

import lombok.Getter;

@Getter
public enum UserType {
    MEMBER(0),
    CAREGIVER(1),
    ;
    final int type;

    UserType(int type){
        this.type = type;
    }

}
