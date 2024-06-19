package com.endside.config.db.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Redis list specific operations 을 사용해 이벤트 큐를 구현
 */
@Repository
public class RedisQueueRepositoryImpl {
    private static final String EVENT_QUEUE_NAME = "event-queue";
    private static final int EVENT_QUEUE_SIZE = 10;
    private final ListOperations<String, String> listOperations;

    public RedisQueueRepositoryImpl(@Qualifier("notiRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.listOperations = redisTemplate.opsForList();
    }

    /**
     * 이벤트 큐 앞쪽에 value를 추가
     * @param value
     */
    public void enqueueEvent(String value) {
        listOperations.leftPush(EVENT_QUEUE_NAME, value);
    }

    /**
     * 이벤트 큐 뒤쪽부터 가능한 최대 EVENT_QUEUE_SIZE 로 정한 만큼 삭제하고 반환
     * @return 큐에 이벤트가 없을 경우 빈 배열 / 큐에 이벤트가 있는경우 최대 EVENT_QUEUE_SIZE 만큼
     */
    public List<String> dequeueEvent() {
        List<String> events = listOperations.rightPop(EVENT_QUEUE_NAME, EVENT_QUEUE_SIZE);
        if (events == null) {
            return Collections.emptyList();
        }
        return events;
    }
}