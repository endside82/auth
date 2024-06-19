package com.endside.user.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class JoinEventHandler {

    @Async
    @EventListener
    public void handle(JoinEvent event) {
        if ( event.getUserId() != null ) {
          // async what ever
        }
    }

}
