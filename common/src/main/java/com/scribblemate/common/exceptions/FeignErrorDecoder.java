package com.scribblemate.common.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scribblemate.common.responses.ErrorResponse;
import com.scribblemate.common.utility.ResponseErrorUtils;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorResponse errorResponse;
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            errorResponse = objectMapper.readValue(body, ErrorResponse.class);
        } catch (IOException e) {
            log.error("Error reading error response body", e);
            throw new FeignClientException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        throw new FeignClientException(HttpStatus.resolve(errorResponse.getMessagecode()), ResponseErrorUtils.fromMessage(errorResponse.getMessage()), errorResponse.getObject().toString());
    }
}
