package com.scribblemate.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {

    @Value("${auth.api.prefix}")
    private String authApiPrefix;

    @Value("${labels.api.prefix}")
    private String labelsApiPrefix;

    @Value("${notes.api.prefix}")
    private String notesApiPrefix;

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Value("${labels.server.url}")
    private String labelsServerUrl;

    @Value("${notes.server.url}")
    private String notesServerUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth_service")
                .route(RequestPredicates.path(authApiPrefix), HandlerFunctions.http(authServerUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> labelsServiceRoute() {
        return GatewayRouterFunctions.route("labels_service")
                .route(RequestPredicates.path(labelsApiPrefix), HandlerFunctions.http(labelsServerUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notesServiceRoute() {
        return GatewayRouterFunctions.route("notes_service")
                .route(RequestPredicates.path(notesApiPrefix), HandlerFunctions.http(notesServerUrl))
                .build();
    }


}
