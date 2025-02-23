package com.scribblemate.services;

import com.scribblemate.common.event.UserEventData;
import com.scribblemate.common.exceptions.KafkaEventException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.entities.SpecificNote;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.LabelRepository;
import com.scribblemate.repositories.SpecificNoteRepository;
import com.scribblemate.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecificNoteRepository specificNoteRepository;

    @Autowired
    private LabelRepository labelRepository;

    public User setUserFromUserEvent(UserEventData userEventData) {
        User user = new User();
        user.setId(userEventData.getId());
        return user;
    }

    @Transactional
    public void persistUserAfterEvent(UserEventData userEventData) {
        try {
            User user = setUserFromUserEvent(userEventData);
            userRepository.save(user);
            log.info(EventUtils.USER_PERSIST_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.USER_PERSIST_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.USER_PERSIST_ERROR_EVENT, exp);
        }
    }

    @Transactional
    public void deleteUserAfterEvent(UserEventData userEventData) {
        try {
            User user = userRepository.findById(userEventData.getId())
                    .orElseThrow(UserNotFoundException::new);
            if (!user.getLabelSet().isEmpty()) {
                user.getLabelSet().forEach(label -> specificNoteRepository.deleteLabelsFromLabelNote(label.getId()));
            }
            labelRepository.deleteAllByUser(user);
            user.getLabelSet().clear();
            userRepository.delete(user);
            log.info(EventUtils.USER_DELETE_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.USER_DELETE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.USER_DELETE_ERROR_EVENT, exp);
        }
    }
}
