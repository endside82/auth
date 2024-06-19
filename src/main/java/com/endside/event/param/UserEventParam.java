package com.endside.event.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEventParam {
    @JsonIgnore
    private long targetId;

    private long timestamp;

    private int limit;
}