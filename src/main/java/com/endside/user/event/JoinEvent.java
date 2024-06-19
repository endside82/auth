package com.endside.user.event;

import com.endside.util.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinEvent extends DomainEvent {
    private Long userId;
    public JoinEvent(Long userId) {
        this.userId = userId;
    }
}
