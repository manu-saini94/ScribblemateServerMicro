package com.scribblemate.services;

import com.scribblemate.common.utility.Utils;
import com.scribblemate.entities.User;
import com.scribblemate.common.event.UserEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaService {

    @Autowired
    private KafkaTemplate<Long, UserEventData> kafkaTemplate;

    private Message<UserEventData> getMessageWithHeadersAndPayload(Utils.KafkaEvent kafkaEventTopic, UserEventData userEventData) {
        Message<UserEventData> message = MessageBuilder
                .withPayload(userEventData)
                .setHeader(KafkaHeaders.TOPIC, kafkaEventTopic.getValue())
                .setHeader(KafkaHeaders.KEY, userEventData.getId())
                .setHeader("user_email", userEventData.getEmail())
                .build();
        return message;
    }

    public void publishUserCreatedEvent(User user) {
        UserEventData userCreatedData = setUserEventDataFromUser(user);
        Message<UserEventData> message = getMessageWithHeadersAndPayload(Utils.KafkaEvent.USER_CREATED, userCreatedData);
        log.info("Start - Sending UserCreatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData, message.toString());
        kafkaTemplate.send(message);
        log.info("End - Sending UserCreatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_CREATED.getValue(), userCreatedData, message.toString());
    }

    public void publishUserUpdatedEvent(User user) {
        UserEventData userUpdatedData = setUserEventDataFromUser(user);
        Message<UserEventData> message = getMessageWithHeadersAndPayload(Utils.KafkaEvent.USER_UPDATED, userUpdatedData);
        log.info("Start - Sending UserUpdatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_UPDATED.getValue(), userUpdatedData, message.toString());
        kafkaTemplate.send(message);
        log.info("End - Sending UserUpdatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_UPDATED.getValue(), userUpdatedData, message.toString());
    }

    public void publishUserDeletedEvent(User user) {
        UserEventData userDeletedData = setUserEventDataFromUser(user);
        Message<UserEventData> message = getMessageWithHeadersAndPayload(Utils.KafkaEvent.USER_DELETED, userDeletedData);
        log.info("Start - Sending UserDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData, message.toString());
        kafkaTemplate.send(message);
        log.info("End - Sending UserDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.USER_DELETED.getValue(), userDeletedData, message.toString());
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
