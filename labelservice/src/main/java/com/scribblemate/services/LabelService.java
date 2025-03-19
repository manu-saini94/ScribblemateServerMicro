package com.scribblemate.services;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.scribblemate.common.dto.NoteLabelDto;
import com.scribblemate.common.event.note.NoteEventData;
import com.scribblemate.common.event.note.NoteLabelEventData;
import com.scribblemate.common.event.note.NoteLabelIdsEventData;
import com.scribblemate.common.exceptions.KafkaEventException;
import com.scribblemate.common.exceptions.LabelsNotFoundException;
import com.scribblemate.common.exceptions.NotAuthorizedException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.ResponseErrorUtils;
import com.scribblemate.common.utility.UserUtils;
import com.scribblemate.entities.User;
import com.scribblemate.exceptions.labels.*;
import com.scribblemate.repositories.UserRepository;
import com.scribblemate.utility.LabelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.dto.LabelDto;
import com.scribblemate.entities.Label;
import com.scribblemate.repositories.LabelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Transactional
    public LabelDto createNewLabel(LabelDto labelDto, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);
            Label foundLabel = labelRepository.findByUserIdAndLabelName(userId, labelDto.getLabelName());
            if (foundLabel != null) {
                log.error(LabelUtils.LABEL_ALREADY_EXIST_ERROR, foundLabel);
                throw new LabelAlreadyExistException();
            }
            Label label = new Label();
            label.setLabelName(labelDto.getLabelName());
            label.setImportant(labelDto.isImportant());
            label.setUser(user);
            Label savedLabel = labelRepository.save(label);
            log.info(LabelUtils.LABEL_PERSIST_SUCCESS, savedLabel);
            kafkaProducerService.publishLabelCreatedEvent(savedLabel, user.getEmail());
            return getLabelDtoFromLabel(savedLabel);
        } catch (UserNotFoundException exception) {
            log.error(UserUtils.ERROR_USER_NOT_FOUND, exception);
            throw exception;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_PERSIST_ERROR, userId, ex.getMessage());
            throw new LabelNotPersistedException(ex);
        }
    }

    @Transactional
    public boolean deleteLabel(Long labelId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        Label label = labelRepository.findByIdAndUserId(labelId, userId).orElseThrow(() -> {
            log.error(LabelUtils.LABEL_NOT_FOUND);
            return new LabelNotFoundException();
        });
        try {
            labelRepository.delete(label);
            log.info(LabelUtils.LABEL_DELETE_SUCCESS, label);
            kafkaProducerService.publishLabelDeletedEvent(label, user.getEmail());
            return true;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_DELETE_ERROR, labelId, ex.getMessage());
            throw new LabelNotDeletedException(ex);
        }
    }

    @Transactional
    public LabelDto editLabel(LabelDto labelDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        Label label = labelRepository.findByIdAndUserId(labelDto.getId(), userId).orElseThrow(() -> {
            log.error(LabelUtils.LABEL_NOT_FOUND);
            return new LabelNotFoundException();
        });
        try {
            label.setLabelName(labelDto.getLabelName());
            label.setImportant(labelDto.isImportant());
            Label savedLabel = labelRepository.save(label);
            log.info(LabelUtils.LABEL_UPDATE_SUCCESS, savedLabel.getId());
            kafkaProducerService.publishLabelUpdatedEvent(savedLabel, user.getEmail());
            return getLabelDtoFromLabel(savedLabel);
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_UPDATE_ERROR, labelDto.getId(), ex.getMessage());
            throw new LabelNotUpdatedException(ex);
        }
    }

    public Set<Label> getAllLabelsByUser(Long userId) {
        try {
            Set<Label> labelSet = labelRepository.findAllByUserIdOrderByLabelName(userId);
            return labelSet;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, ex.getMessage());
            throw new LabelsNotFoundException(ex);
        }
    }

    public Set<LabelDto> getLabelsByUser(Long userId) {
        try {
            Set<Label> labelList = getAllLabelsByUser(userId);
            log.info(LabelUtils.LABEL_FETCH_SUCCESS, userId);
            Set<LabelDto> labelDtoSet = labelList.stream().map(label -> getLabelDtoFromLabel(label)).collect(Collectors.toSet());
            return labelDtoSet;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, ex.getMessage());
            throw new LabelsNotFoundException(ex);
        }
    }

    public Map<Long, Set<Long>> getLabelsByNoteIds(Long userId) {
        try {
            Set<Label> labelList = getAllLabelsByUser(userId);
            log.info(LabelUtils.LABEL_FETCH_SUCCESS, userId);
            Set<Long> distinctNoteIds = labelList.stream()
                    .flatMap(label -> label.getNoteIds().stream())
                    .collect(Collectors.toSet());
            Map<Long, Set<Long>> labelsByNotesMap = distinctNoteIds.stream()
                    .collect(Collectors.toMap(noteId -> noteId, noteId -> labelList.stream()
                            .filter(label -> label.getNoteIds()
                                    .contains(noteId))
                            .map(Label::getId).collect(Collectors.toSet())));
            return labelsByNotesMap;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, ex.getMessage());
            throw new LabelsNotFoundException(ex);
        }
    }


    private LabelDto getLabelDtoFromLabel(Label savedLabel) {
        LabelDto labelDto = new LabelDto();
        labelDto.setId(savedLabel.getId());
        labelDto.setLabelName(savedLabel.getLabelName());
        labelDto.setImportant(savedLabel.isImportant());
        labelDto.setNoteIds(savedLabel.getNoteIds());
        labelDto.setCreatedAt(savedLabel.getCreatedAt());
        if (savedLabel.getCreatedAt().compareTo(savedLabel.getUpdatedAt()) != 0) {
            labelDto.setUpdatedAt(savedLabel.getUpdatedAt());
        }
        return labelDto;
    }

    @Transactional
    public NoteLabelDto addLabelListInNote(Set<Long> labelIds, Long noteId, Long userId) {
        try {
            NoteLabelDto noteLabelDto = new NoteLabelDto();
            Long labelId = labelIds.stream().findFirst().get();
            Label label = labelRepository.findByIdAndUserId(labelId, userId).orElseThrow(() -> {
                log.error(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
                return new NotAuthorizedException();
            });
            if (label.getUser().getId() == userId) {
                labelRepository.addLabelIdsToNote(labelIds, noteId);
                log.info(LabelUtils.LABEL_PERSIST_SUCCESS);
                Set<Long> noteLabelIds = labelRepository.findLabelIdsByNoteId(noteId);
                log.info(LabelUtils.LABEL_FETCH_SUCCESS);
                noteLabelDto.setNoteId(noteId);
                noteLabelDto.setLabelList(noteLabelIds);
            }
            return noteLabelDto;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_PERSIST_ERROR, noteId, ex.getMessage());
            throw new LabelNotPersistedException(ex);
        }
    }

    @Transactional
    public Boolean addLabelInNote(Long labelId, Long noteId, Long userId) {

        try {
            Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundException());
            if (label.getUser().getId() == userId) {
                labelRepository.addLabelToNote(labelId, noteId);
                log.info(LabelUtils.LABEL_PERSIST_SUCCESS);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_PERSIST_ERROR, labelId, ex.getMessage());
            throw new LabelNotPersistedException(ex);
        }
    }

    @Transactional
    public Boolean deleteLabelInNote(Long labelId, Long noteId, Long userId) {
        try {
            Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundException());
            if (label.getUser().getId() == userId) {
                labelRepository.deleteLabelFromNote(labelId, noteId);
                log.info(LabelUtils.LABEL_DELETE_SUCCESS);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_DELETE_ERROR, labelId, ex.getMessage());
            throw new LabelNotDeletedException(ex);
        }
    }


    public Set<Long> getAllNoteIdsWithLabelsByUser(Long userId) {
        try {
            Set<Label> labelSet = getAllLabelsByUser(userId);
            Set<Long> distinctNoteIds = labelSet.stream().flatMap(label -> label.getNoteIds().stream()).collect(Collectors.toSet());
            log.info(LabelUtils.LABEL_FETCH_SUCCESS, userId);
            return distinctNoteIds;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, ex.getMessage());
            throw new LabelsNotFoundException(ex);
        }
    }

    @Transactional
    public Boolean deleteAllLabelsForNote(Long noteId, Long userId) {
        try {
            labelRepository.deleteLabelIdsByNoteId(noteId, userId);
            return true;
        } catch (Exception exp) {
            log.error(LabelUtils.LABEL_DELETE_ERROR, userId, exp.getMessage());
            throw new LabelNotDeletedException(exp);
        }
    }
    @Transactional
    private Boolean deleteAllNoteReferences(Long noteId) {
        try {
            labelRepository.deleteNoteReferences(noteId);
            return true;
        } catch (Exception exp) {
            log.error(EventUtils.NOTE_DELETE_ERROR_EVENT, noteId, exp.getMessage());
            throw new LabelNotDeletedException(exp);
        }
    }

    /**
     * Kafka method for assigning labels to note
     * @param eventData
     * @param userEmail
     */
    @Transactional
    public void assignLabelIdsInNote(NoteLabelIdsEventData eventData, String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> {
                log.error(UserUtils.ERROR_USER_NOT_FOUND, userEmail);
                return new UserNotFoundException();
            });
            addLabelListInNote(eventData.getLabelIds(), eventData.getId(), user.getId());
            log.info(EventUtils.LABEL_PERSIST_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.LABEL_PERSIST_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.LABEL_PERSIST_ERROR_EVENT, exp);
        }
    }

    /**
     * Kafka method for assigning single label to note
     * @param eventData
     * @param userEmail
     */
    @Transactional
    public void assignLabelInNote(NoteLabelEventData eventData, String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> {
                log.error(UserUtils.ERROR_USER_NOT_FOUND, userEmail);
                return new UserNotFoundException();
            });
            addLabelInNote(eventData.getLabelId(), eventData.getId(), user.getId());
            log.info(EventUtils.LABEL_PERSIST_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.LABEL_PERSIST_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.LABEL_PERSIST_ERROR_EVENT, exp);
        }
    }

    /**
     * Kafka method for unassigning single label from note
     * @param eventData
     * @param userEmail
     */
    @Transactional
    public void unassignLabelInNote(NoteLabelEventData eventData, String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> {
                log.error(UserUtils.ERROR_USER_NOT_FOUND, userEmail);
                return new UserNotFoundException();
            });
            deleteLabelInNote(eventData.getLabelId(), eventData.getId(), user.getId());
            log.info(EventUtils.LABEL_DELETE_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.LABEL_DELETE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.LABEL_DELETE_ERROR_EVENT, exp);
        }
    }

    /**
     * Kafka method for deleting note references
     * @param noteData
     */
    @Transactional
    public void deleteNoteAfterEvent(NoteEventData noteData,String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> {
                log.error(UserUtils.ERROR_USER_NOT_FOUND, userEmail);
                return new UserNotFoundException();
            });
            deleteAllNoteReferences(noteData.getId());
            log.info(EventUtils.NOTE_DELETE_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.NOTE_DELETE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.NOTE_DELETE_ERROR_EVENT, exp);
        }
    }


}
