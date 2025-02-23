package com.scribblemate.services;

import com.scribblemate.common.event.UserEventData;
import com.scribblemate.common.utility.Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaListenerService {
    @Autowired
    private UserService userService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 1))
    @KafkaListener(topics = "USER_CREATED", groupId = "${spring.kafka.consumer.group-id}")
    public void listenForUserCreation(@Payload UserEventData userEventData) {
        log.info("Message received from " + Utils.KafkaEvent.USER_CREATED.getValue() + " topic => {}", userEventData);
        userService.persistUserAfterEvent(userEventData);
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 1))
    @KafkaListener(topics = "USER_DELETED", groupId = "${spring.kafka.consumer.group-id}")
    public void listenForUserDeletion(@Payload UserEventData userEventData) {
        log.info("Message received from " + Utils.KafkaEvent.USER_DELETED.getValue() + " topic => {}", userEventData);
        userService.deleteUserAfterEvent(userEventData);
    }

}
