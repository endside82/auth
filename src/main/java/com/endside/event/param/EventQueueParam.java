package com.endside.event.param;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EventQueueParam {
    private String source;

    private String type;

    private long senderId;

    private long targetId;

    private int targetType;

    private List<Long> receivers;

    private String extData;

    private long timestamp;

    // TODO: delete when using test event producer method is done
    @Builder
    public EventQueueParam(String source, String type, long senderId, long targetId, int targetType, List<Long> receivers, String extData, long timestamp) {
        this.source = source;
        this.type = type;
        this.senderId = senderId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.receivers = receivers;
        this.extData = extData;
        this.timestamp = timestamp;
    }
}