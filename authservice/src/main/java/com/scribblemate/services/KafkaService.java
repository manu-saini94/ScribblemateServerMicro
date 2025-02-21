package com.scribblemate.services;

import com.scribblemate.common.utility.Utils;
import com.scribblemate.entities.User;
import com.scribblemate.user.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaService {

    @Autowired
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public void publishUserCreatedEvent(User user) {
        UserCreatedEvent userCreatedEvent = setUserCreatedEventFromUser(user);
        log.info("Start - Sending UserCreatedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedEvent);
        kafkaTemplate.send(Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedEvent);
        log.info("End - Sending UserCreatedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedEvent);
    }

    private UserCreatedEvent setUserCreatedEventFromUser(User user) {
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setId(user.getId());
        userCreatedEvent.setFullName(user.getFullName());
        userCreatedEvent.setEmail(user.getEmail());
        userCreatedEvent.setStatus(user.getStatus());
        userCreatedEvent.setProfilePicture(user.getProfilePicture());
        userCreatedEvent.setCreatedAt(user.getCreatedAt());
        userCreatedEvent.setUpdatedAt(user.getUpdatedAt());
        return userCreatedEvent;
    }
}
