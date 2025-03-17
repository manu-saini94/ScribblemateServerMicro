package com.scribblemate.services;

import com.scribblemate.common.event.user.UserEventData;
import com.scribblemate.common.exceptions.KafkaEventException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.UserUtils;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.LabelRepository;
import com.scribblemate.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    public User setUserFromUserEvent(UserEventData userEventData) {
        User user = new User();
        user.setId(userEventData.getId());
        user.setEmail(userEventData.getEmail());
        return user;
    }


    @Transactional
    public void persistUserAfterEvent(UserEventData userEventData) {
        try {
            if (userRepository.existsById(userEventData.getId())) {
                log.error(UserUtils.USER_ALREADY_EXIST);
                return;
            }
            User user = setUserFromUserEvent(userEventData);
            userRepository.save(user);
            log.info(EventUtils.USER_PERSIST_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.USER_PERSIST_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.USER_PERSIST_ERROR_EVENT, exp);
        }
    }


    @Transactional
    public void updateUserAfterEvent(UserEventData userEventData) {
        try {
            User user = userRepository.findById(userEventData.getId())
                    .orElseThrow(UserNotFoundException::new);
            user.setEmail(userEventData.getEmail());
            userRepository.save(user);
            log.info(EventUtils.USER_UPDATE_SUCCESS_EVENT);
        } catch (UserNotFoundException ex) {
            log.error(UserUtils.ERROR_USER_NOT_FOUND);
            return;
        } catch (Exception exp) {
            log.error(EventUtils.USER_UPDATE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.USER_UPDATE_ERROR_EVENT, exp);
        }
    }

    @Transactional
    public void deleteUserAfterEvent(UserEventData userEventData) {
        try {
            User user = userRepository.findById(userEventData.getId())
                    .orElseThrow(UserNotFoundException::new);
            labelRepository.deleteAllByUser(user);
            userRepository.delete(user);
            log.info(EventUtils.USER_DELETE_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.USER_DELETE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.USER_DELETE_ERROR_EVENT, exp);
        }
    }
}
