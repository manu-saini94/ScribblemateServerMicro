package com.scribblemate.services;

import com.google.gson.Gson;
import com.scribblemate.common.event.user.UserEventData;
import com.scribblemate.common.utility.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class KafkaListenerService {

    @Autowired
    private UserService userService;


    @KafkaListener(topics = "USER_CREATED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUserCreation(@Payload ConsumerRecord<String, String> userEventData,
                                      @Header("user_email") String userEmail,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        Gson gson = new Gson();
        UserEventData userData = gson.fromJson(userEventData.value(), UserEventData.class);
        log.info("Message received from " + Utils.KafkaEvent.USER_CREATED.getValue() + " topic => {}", userData);
        userService.persistUserAfterEvent(userData);
    }

    @KafkaListener(topics = "USER_UPDATED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUserUpdation(@Payload ConsumerRecord<String, String> userEventData,
                                      @Header("user_email") String userEmail,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        Gson gson = new Gson();
        UserEventData userData = gson.fromJson(userEventData.value(), UserEventData.class);
        log.info("Message received from " + Utils.KafkaEvent.USER_UPDATED.getValue() + " topic => {}", userData);
        userService.updateUserAfterEvent(userData);
    }

    @KafkaListener(topics = "USER_DELETED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUserDeletion(@Payload ConsumerRecord<String, String> userEventData,
                                      @Header("user_email") String userEmail,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        Gson gson = new Gson();
        UserEventData userData = gson.fromJson(userEventData.value(), UserEventData.class);
        log.info("Message received from " + Utils.KafkaEvent.USER_DELETED.getValue() + " topic => {}", userData);
        userService.deleteUserAfterEvent(userData);
    }

}
