package com.scribblemate.common.utility;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Utils {

    public static final String KAFKA_TYPE_ID = "__TypeId__";

    public enum Role {
        OWNER, COLLABORATOR;

        public static Role findByName(String name) {
            for (Role role : values()) {
                if (role.name().equalsIgnoreCase(name)) {
                    return role;
                }
            }
            return null;
        }
    }

    public enum Status {
        ACTIVE, INACTIVE;

        public static Status findByName(String name) {
            for (Status status : values()) {
                if (status.name().equalsIgnoreCase(name)) {
                    return status;
                }
            }
            return null;
        }
    }

    public enum TokenType {
        ACCESS_TOKEN("accessToken"), REFRESH_TOKEN("refreshToken");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum KafkaTopicTypeId {
        USER("user"),
        NOTE("note"),
        LABEL("label");
        private final String value;

        KafkaTopicTypeId(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum KafkaEvent {
        // User events
        USER_CREATED("USER_CREATED"),
        USER_UPDATED("USER_UPDATED"),
        USER_DELETED("USER_DELETED"),


        // Note events
        NOTE_CREATED("NOTE_CREATED"),
        NOTE_UPDATED("NOTE_UPDATED"),
        NOTE_DELETED("NOTE_DELETED"),

        // Label events
        LABEL_CREATED("LABEL_CREATED"),
        LABEL_UPDATED("LABEL_UPDATED"),
        LABEL_DELETED("LABEL_DELETED");

        private final String value;

        KafkaEvent(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
