package com.scribblemate.aspect;

import com.scribblemate.entities.User;
import com.scribblemate.exceptions.UserNotFoundException;
import com.scribblemate.repositories.UserRepository;
import com.scribblemate.utility.ResponseErrorUtils;
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
public class UserContextAspect {

    @Autowired
    private UserRepository userRepository;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
    }

    @Before("restControllerMethods()")
    public void loadUserContext(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User principal = (User) authentication.getPrincipal();
            Long userId = Long.valueOf(principal.getId());
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with this id : " + userId));
            UserContext.setCurrentUser(user);
        }else{
            throw new AccessDeniedException(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
        }
    }

    @After("restControllerMethods()")
    public void clearUserContext(JoinPoint joinPoint) {
        UserContext.clear();
    }
}
