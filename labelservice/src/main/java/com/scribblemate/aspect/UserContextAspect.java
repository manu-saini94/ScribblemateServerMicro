package com.scribblemate.aspect;

import com.scribblemate.common.utility.ResponseErrorUtils;
import com.scribblemate.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UserContextAspect {

    @Pointcut("@annotation(com.scribblemate.annotation.LoadUserContext)")
    public void restControllerMethods() {
    }

    @Before("restControllerMethods()")
    public void loadUserContext(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User principal = (User) authentication.getPrincipal();
            UserContext.setCurrentUser(principal);
        } else {
            log.error(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
            throw new AccessDeniedException(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
        }
    }

    @After("restControllerMethods()")
    public void clearUserContext(JoinPoint joinPoint) {
        UserContext.clear();
    }
}
