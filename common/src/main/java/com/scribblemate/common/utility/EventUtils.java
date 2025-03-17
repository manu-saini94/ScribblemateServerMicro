package com.scribblemate.common.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scribblemate.common.adapter.LocalDateTimeAdapter;
import com.scribblemate.common.event.user.UserEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;

@Slf4j
public class EventUtils {

    public static final String USER_PERSIST_SUCCESS_EVENT = "User persisted successfully after event consumed";

    public static final String USER_UPDATE_SUCCESS_EVENT = "User updated successfully after event consumed";

    public static final String USER_DELETE_SUCCESS_EVENT = "User deleted successfully after event consumed";

    public static final String USER_PERSIST_ERROR_EVENT = "User persist error after event consumed";

    public static final String USER_UPDATE_ERROR_EVENT = "User update error after event consumed";

    public static final String USER_DELETE_ERROR_EVENT = "User delete error after event consumed";

    public static final String LABEL_PERSIST_SUCCESS_EVENT = "Label persisted successfully after event consumed";
    public static final String LABEL_PERSIST_ERROR_EVENT = "Label persist error after event consumed";
    public static final String LABEL_DELETE_SUCCESS_EVENT = "Label deleted successfully after event consumed";
    public static final String LABEL_DELETE_ERROR_EVENT = "Label delete error after event consumed";
    public static final String LABEL_UPDATE_SUCCESS_EVENT = "Label updated successfully after event consumed";
    public static final String LABEL_UPDATE_ERROR_EVENT = "Label update error after event consumed";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public static <T> Message<T> getMessageWithHeadersAndPayload(Utils.KafkaEvent kafkaEventTopic,
                                                                 Long key, T eventData, String email) {
        Message<T> message = MessageBuilder
                .withPayload(eventData)
                .setHeader(KafkaHeaders.TOPIC, kafkaEventTopic.getValue())
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("user_email", email)
                .build();
        return message;
    }


    public static <T> T getEventDataFromJson(String jsonString, Class<T> targetClass) {
        T eventObject = gson.fromJson(jsonString, targetClass);
        return eventObject;
    }


    public static <T> void logEventData(T eventObject, Utils.KafkaEvent kafkaEvent) {
        log.info("Message received from " + kafkaEvent.getValue() + " topic => {}", eventObject);
    }

}
