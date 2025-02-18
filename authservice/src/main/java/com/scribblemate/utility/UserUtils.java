package com.scribblemate.utility;

public class UserUtils {

    public static final String ERROR_PERSISTING_USER = "Error occurred while persisting user: {}";
    public static final String ERROR_USER_NOT_FOUND = "User not found";

    public static final String LOGIN_URI = "/login";

    public static final String REGISTER_URI = "/signup";

    public static final String FORGOT_URI = "/forgot";

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


}
