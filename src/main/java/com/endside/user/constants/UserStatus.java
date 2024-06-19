package com.endside.user.constants;

import lombok.Getter;

@Getter
public enum UserStatus {
    NORMAL(0, "normal"),
    LOGOUT(1, "logout"),
    STOP(2, "stop"),        // 이상 시스템 감지 의한 정지 상황
    BAN(3, "ban"),          // 명시적으로 접근을 금지함
    TRYEXIT(4,"tryexit"),
    EXIT(5, "exit");        // 탈퇴
    final int status;
    final String statusName;
    UserStatus(int status, String statusName){
        this.status = status;
        this.statusName = statusName;
    }
}
