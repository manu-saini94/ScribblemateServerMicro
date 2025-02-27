package com.scribblemate.services;

import com.scribblemate.client.AuthClient;
import com.scribblemate.common.dto.UserDto;
import com.scribblemate.common.responses.SuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApiGatewayService {

    @Autowired
    private AuthClient authClient;

    public ResponseEntity<SuccessResponse<UserDto>> authenticateUser() {
        log.info("Inside Api Gateway service for authentication");
        ResponseEntity<SuccessResponse<UserDto>> authResponse = authClient.authenticatedUser();
        return authResponse;
    }
}
