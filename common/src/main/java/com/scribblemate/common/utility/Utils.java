package com.scribblemate.common.utility;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Utils {

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

    public enum KafkaEvent {
        // User events
        USER_CREATED("user_created"),
        USER_UPDATED("user_updated"),
        USER_DELETED("user_deleted"),


        // Note events
        NOTE_CREATED("note_created"),
        NOTE_UPDATED("note_updated"),
        NOTE_DELETED("note_deleted"),

        // Label events
        LABEL_CREATED("label_created"),
        LABEL_UPDATED("label_updated"),
        LABEL_DELETED("label_deleted");

        private final String value;

        KafkaEvent(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
