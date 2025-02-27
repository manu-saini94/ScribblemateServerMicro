package com.scribblemate.client;

import com.scribblemate.common.dto.UserDto;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.configuration.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@FeignClient(name = "auth-service",
        url = "${auth.server.url}${auth.api.prefix}",
        configuration = FeignClientConfiguration.class)
public interface AuthClient {

    @GetMapping(path = "/authenticate")
    public ResponseEntity<SuccessResponse<UserDto>> authenticatedUser();

}
