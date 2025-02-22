package com.scribblemate.services;

import com.scribblemate.common.utility.Utils;
import com.scribblemate.entities.User;
import com.scribblemate.common.user.event.UserEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaService {

    @Autowired
    private KafkaTemplate<String, UserEventData> kafkaTemplate;

    public void publishUserCreatedEvent(User user) {
        UserEventData userCreatedData = setUserEventDataFromUser(user);
        log.info("Start - Sending UserCreatedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData);
        kafkaTemplate.send(Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData);
        log.info("End - Sending UserCreatedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData);
    }

    public void publishUserUpdatedEvent(User user){
        UserEventData userUpdatedData = setUserEventDataFromUser(user);
        log.info("Start - Sending UserUpdatedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userUpdatedData);
        kafkaTemplate.send(Utils.KafkaEvent.USER_UPDATED.getValue(), userUpdatedData);
        log.info("End - Sending UserUpdatedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userUpdatedData);
    }

    public void publishUserDeletedEvent(User user){
        UserEventData userDeletedData = setUserEventDataFromUser(user);
        log.info("Start - Sending UserDeletedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData);
        kafkaTemplate.send(Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData);
        log.info("End - Sending UserDeletedEvent {} to Kafka Topic " + Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData);
    }

    private UserEventData setUserEventDataFromUser(User user) {
        UserEventData userEventData = new UserEventData();
        userEventData.setId(user.getId());
        userEventData.setFullName(user.getFullName());
        userEventData.setEmail(user.getEmail());
        userEventData.setStatus(user.getStatus());
        userEventData.setProfilePicture(user.getProfilePicture());
        userEventData.setCreatedAt(user.getCreatedAt());
        userEventData.setUpdatedAt(user.getUpdatedAt());
        return userEventData;
    }
}
