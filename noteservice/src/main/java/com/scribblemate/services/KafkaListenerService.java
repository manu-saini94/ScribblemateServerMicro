package com.scribblemate.services;

import com.scribblemate.common.utility.Utils;
import com.scribblemate.user.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaListenerService {

    @KafkaListener(topics = "USER_CREATED")
    public void listenForUserCreation(UserCreatedEvent userCreatedEvent) {
        log.info("Message received from " + Utils.KafkaEvent.USER_CREATED.getValue() + " topic => {}", userCreatedEvent);
    }
}
