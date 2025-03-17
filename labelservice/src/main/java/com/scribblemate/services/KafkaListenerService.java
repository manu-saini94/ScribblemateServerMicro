package com.scribblemate.services;

import com.scribblemate.common.event.note.NoteLabelEventData;
import com.scribblemate.common.event.note.NoteLabelIdsEventData;
import com.scribblemate.common.event.user.UserEventData;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class KafkaListenerService {

    @Autowired
    private UserService userService;

    @Autowired
    private LabelService labelService;


    @KafkaListener(topics = "USER_CREATED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUserCreation(@Payload ConsumerRecord<String, String> consumerRecord,
                                      @Header("user_email") String userEmail,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UserEventData userData = EventUtils.getEventDataFromJson(consumerRecord.value(), UserEventData.class);
        EventUtils.logEventData(userData, Utils.KafkaEvent.USER_CREATED);
        String decodedKey = StringEscapeUtils.unescapeJava(key);
        log.info("Message Headers( key : " + decodedKey + "user_email : " + userEmail + " )");
        userService.persistUserAfterEvent(userData);
    }

    @KafkaListener(topics = "USER_UPDATED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUserUpdation(@Payload ConsumerRecord<String, String> consumerRecord,
                                      @Header("user_email") String userEmail,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UserEventData userData = EventUtils.getEventDataFromJson(consumerRecord.value(), UserEventData.class);
        EventUtils.logEventData(userData, Utils.KafkaEvent.USER_UPDATED);
        String decodedKey = StringEscapeUtils.unescapeJava(key);
        log.info("Message Headers( key : " + decodedKey + "user_email : " + userEmail + " )");
        userService.updateUserAfterEvent(userData);
    }

    @KafkaListener(topics = "USER_DELETED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUserDeletion(@Payload ConsumerRecord<String, String> consumerRecord,
                                      @Header("user_email") String userEmail,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UserEventData userData = EventUtils.getEventDataFromJson(consumerRecord.value(), UserEventData.class);
        EventUtils.logEventData(userData, Utils.KafkaEvent.USER_DELETED);
        String decodedKey = StringEscapeUtils.unescapeJava(key);
        log.info("Message Headers( key : " + decodedKey + "user_email : " + userEmail + " )");
        userService.deleteUserAfterEvent(userData);
    }

    @KafkaListener(topics = "NOTE_LABELS_ASSIGNED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForAssignLabelIdsToNote(@Payload ConsumerRecord<String, String> consumerRecord,
                                              @Header("user_email") String userEmail,
                                              @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        NoteLabelIdsEventData noteLabelsEventData = EventUtils.getEventDataFromJson(consumerRecord.value(), NoteLabelIdsEventData.class);
        EventUtils.logEventData(noteLabelsEventData, Utils.KafkaEvent.NOTE_LABELS_ASSIGNED);
        String decodedKey = StringEscapeUtils.unescapeJava(key);
        log.info("Message Headers( key : " + decodedKey + "user_email : " + userEmail + " )");
        labelService.assignLabelIdsInNote(noteLabelsEventData,userEmail);
    }


    @KafkaListener(topics = "NOTE_LABEL_ASSIGNED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForAssignLabelToNote(@Payload ConsumerRecord<String, String> consumerRecord,
                                              @Header("user_email") String userEmail,
                                              @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        NoteLabelEventData noteLabelEventData = EventUtils.getEventDataFromJson(consumerRecord.value(), NoteLabelEventData.class);
        EventUtils.logEventData(noteLabelEventData, Utils.KafkaEvent.NOTE_LABEL_ASSIGNED);
        String decodedKey = StringEscapeUtils.unescapeJava(key);
        log.info("Message Headers( key : " + decodedKey + "user_email : " + userEmail + " )");
        labelService.assignLabelInNote(noteLabelEventData,userEmail);
    }

    @KafkaListener(topics = "NOTE_LABEL_UNASSIGNED", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenForUnassignLabelInNote(@Payload ConsumerRecord<String, String> consumerRecord,
                                           @Header("user_email") String userEmail,
                                           @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        NoteLabelEventData noteLabelEventData = EventUtils.getEventDataFromJson(consumerRecord.value(), NoteLabelEventData.class);
        EventUtils.logEventData(noteLabelEventData, Utils.KafkaEvent.NOTE_LABEL_UNASSIGNED);
        String decodedKey = StringEscapeUtils.unescapeJava(key);
        log.info("Message Headers( key : " + decodedKey + "user_email : " + userEmail + " )");
        labelService.unassignLabelInNote(noteLabelEventData,userEmail);
    }


}
