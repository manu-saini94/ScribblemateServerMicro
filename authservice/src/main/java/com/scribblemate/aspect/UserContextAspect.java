package com.scribblemate.aspect;

import com.scribblemate.entities.User;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.repositories.UserRepository;
import com.scribblemate.common.utility.ResponseErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UserContextAspect {

    @Autowired
    private UserRepository userRepository;

    @Pointcut("@annotation(com.scribblemate.annotation.LoadUserContext)")
    public void restControllerMethods() {
    }

    @Before("restControllerMethods()")
    public void loadUserContext(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User principal = (User) authentication.getPrincipal();
            String userEmail = principal.getEmail();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + userEmail));
            UserContext.setCurrentUser(user);
        }else{
            log.error(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
            throw new AccessDeniedException(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
        }
    }

    @After("restControllerMethods()")
    public void clearUserContext(JoinPoint joinPoint) {
        UserContext.clear();
    }
}
