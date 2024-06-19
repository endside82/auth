package com.endside.main.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Now {
    private long time;
    public Now(long time) {
        this.time = time;
    }
}
