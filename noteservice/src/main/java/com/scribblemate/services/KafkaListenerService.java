package com.scribblemate.services;

import com.scribblemate.common.user.event.UserEventData;
import com.scribblemate.common.utility.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaListenerService {

    @KafkaListener(topics = "USER_CREATED")
    public void listenForUserCreation(UserEventData userEventData) {
        log.info("Message received from " + Utils.KafkaEvent.USER_CREATED.getValue() + " topic => {}", userEventData);
    }

    @KafkaListener(topics = "USER_UPDATED")
    public void listenForUserUpdation(UserEventData userEventData) {
        log.info("Message received from " + Utils.KafkaEvent.USER_UPDATED.getValue() + " topic => {}", userEventData);
    }

    @KafkaListener(topics = "USER_DELETED")
    public void listenForUserDeletion(UserEventData userEventData) {
        log.info("Message received from " + Utils.KafkaEvent.USER_DELETED.getValue() + " topic => {}", userEventData);
//        if (!user.getLabelSet().isEmpty()) {
//				user.getLabelSet().forEach(label -> specificNoteRepository.deleteLabelsFromLabelNote(label.getId()));
//			}
//			labelRepository.deleteAllByUser(user);
//			user.getNoteList().forEach(note -> {
//				if (!note.getCollaboratorList().isEmpty()) {
//					List<User> userList = note.getCollaboratorList().stream().filter(item -> !item.equals(user))
//							.toList();
//					note.setCollaboratorList(userList);
//				}
//				if (!note.getSpecificNoteList().isEmpty()) {
//					List<SpecificNote> noteList = note.getSpecificNoteList().stream()
//							.filter(item -> !item.getUser().equals(user)).toList();
//					note.setSpecificNoteList(noteList);
//				}
//				noteRepository.save(note);
//			});
//			user.getLabelSet().clear();
    }
}
