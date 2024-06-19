package com.endside.event.constant;

import lombok.Getter;

@Getter
public enum TargetType {
    SINGLE(0),
    GROUP(1),
    PLURAL(2)
    ;

    private final int targetType;

    TargetType(int targetType) {
        this.targetType = targetType;
    }
}