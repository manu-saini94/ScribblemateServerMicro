package com.scribblemate.services;

import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.Utils;
import com.scribblemate.entities.User;
import com.scribblemate.common.event.user.UserEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<Long, UserEventData> kafkaTemplate;


    public void publishUserCreatedEvent(User user) {
        UserEventData userCreatedData = setUserEventDataFromUser(user);
        Message<UserEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.USER_CREATED, userCreatedData.getId(), userCreatedData, userCreatedData.getEmail());
        log.info("Start - Sending UserCreatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData, message);
        kafkaTemplate.send(message);
        log.info("End - Sending UserCreatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData, message);
    }

    public void publishUserUpdatedEvent(User user) {
        UserEventData userUpdatedData = setUserEventDataFromUser(user);
        Message<UserEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.USER_UPDATED, userUpdatedData.getId(), userUpdatedData, userUpdatedData.getEmail());
        log.info("Start - Sending UserUpdatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_UPDATED.getValue(), userUpdatedData, message);
        kafkaTemplate.send(message);
        log.info("End - Sending UserUpdatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_UPDATED.getValue(), userUpdatedData, message);
    }

    public void publishUserDeletedEvent(User user) {
        UserEventData userDeletedData = setUserEventDataFromUser(user);
        Message<UserEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.USER_DELETED, userDeletedData.getId(), userDeletedData, userDeletedData.getEmail());
        log.info("Start - Sending UserDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData, message);
        kafkaTemplate.send(message);
        log.info("End - Sending UserDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData, message);
    }

    private UserEventData setUserEventDataFromUser(User user) {
        UserEventData userEventData = new UserEventData();
        userEventData.setId(user.getId());
        userEventData.setFullName(user.getFullName());
        userEventData.setEmail(user.getEmail());
        userEventData.setStatus(user.getStatus());
        userEventData.setProfilePicture(user.getProfilePicture());
        return userEventData;
    }
}
