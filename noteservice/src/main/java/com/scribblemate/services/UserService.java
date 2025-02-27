package com.scribblemate.services;

import com.scribblemate.common.event.UserEventData;
import com.scribblemate.common.exceptions.KafkaEventException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.entities.SpecificNote;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.LabelRepository;
import com.scribblemate.repositories.NoteRepository;
import com.scribblemate.repositories.SpecificNoteRepository;
import com.scribblemate.repositories.UserRepository;
import com.scribblemate.common.utility.EventUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final SpecificNoteRepository specificNoteRepository;

    private final NoteRepository noteRepository;

    private final LabelRepository labelRepository;

    @Transactional
    public User setUserFromUserEvent(UserEventData userEventData) {
        User user = new User();
        user.setFullName(userEventData.getFullName());
        user.setEmail(userEventData.getEmail());
        user.setProfilePicture(userEventData.getProfilePicture());
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
    public void updateUserAfterEvent(UserEventData userEventData) {
        try {
            User user = userRepository.findById(userEventData.getId())
                    .orElseThrow(UserNotFoundException::new);
            user.setFullName(userEventData.getFullName());
            user.setEmail(userEventData.getEmail());
            user.setProfilePicture(userEventData.getProfilePicture());
            userRepository.save(user);
            log.info(EventUtils.USER_UPDATE_SUCCESS_EVENT);
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
            if (!user.getLabelSet().isEmpty()) {
                user.getLabelSet().forEach(label -> specificNoteRepository.deleteLabelsFromLabelNote(label.getId()));
            }
            labelRepository.deleteAllByUser(user);
            user.getNoteList().forEach(note -> {
                if (!note.getCollaboratorList().isEmpty()) {
                    List<User> userList = note.getCollaboratorList().stream().filter(item -> !item.equals(user))
                            .toList();
                    note.setCollaboratorList(userList);
                }
                if (!note.getSpecificNoteList().isEmpty()) {
                    List<SpecificNote> noteList = note.getSpecificNoteList().stream()
                            .filter(item -> !item.getUser().equals(user)).toList();
                    note.setSpecificNoteList(noteList);
                }
                noteRepository.save(note);
            });
            user.getLabelSet().clear();
            userRepository.delete(user);
            log.info(EventUtils.USER_DELETE_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.USER_DELETE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.USER_DELETE_ERROR_EVENT, exp);
        }
    }

}
