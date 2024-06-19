package com.endside.event.constant;

import lombok.Getter;

@Getter
public enum UserEventType {
    USER_LEAVE("user.leave")
    ;

    private final String userEventType;
    UserEventType(String userEventType) {
        this.userEventType = userEventType;
    }

    @Override
    public String toString() {
        return this.userEventType;
    }
}