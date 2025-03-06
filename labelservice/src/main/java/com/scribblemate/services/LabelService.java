package com.scribblemate.services;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.scribblemate.common.dto.NoteLabelDto;
import com.scribblemate.common.exceptions.NotAuthorizedException;
import com.scribblemate.common.utility.ResponseErrorUtils;
import com.scribblemate.exceptions.labels.*;
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

    @Transactional
    public LabelDto createNewLabel(LabelDto labelDto, Long userId) {
        try {
            Label label = new Label();
            label.setLabelName(labelDto.getLabelName());
            label.setImportant(labelDto.isImportant());
            label.setUserId(userId);
            Label foundLabel = labelRepository.findByUserIdAndLabelName(userId, labelDto.getLabelName());
            if (foundLabel != null) {
                log.error(LabelUtils.LABEL_ALREADY_EXIST_ERROR, label);
                throw new LabelAlreadyExistException();
            }
            Label savedLabel = labelRepository.save(label);
            log.info(LabelUtils.LABEL_PERSIST_SUCCESS, savedLabel);
            return getLabelDtoFromLabel(savedLabel);
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_PERSIST_ERROR, new LabelNotPersistedException(ex.getMessage()));
            throw new LabelNotPersistedException(ex.getMessage());
        }
    }

    @Transactional
    public boolean deleteLabel(Long labelId, Long userId) {
        try {
            labelRepository.deleteByIdAndUserId(labelId, userId);
            log.info(LabelUtils.LABEL_DELETE_SUCCESS, labelId);
            return true;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_DELETE_ERROR, labelId, new LabelNotDeletedException(ex.getMessage()));
            throw new LabelNotDeletedException(ex.getMessage());
        }
    }

    @Transactional
    public LabelDto editLabel(LabelDto labelDto, Long userId) {

        try {
            Label label = labelRepository.findByIdAndUserId(labelDto.getId(), userId).orElseThrow(() -> {
                log.error(LabelUtils.LABEL_NOT_FOUND);
                return new LabelNotFoundException();
            });
            label.setLabelName(labelDto.getLabelName());
            label.setImportant(labelDto.isImportant());
            Label savedLabel = labelRepository.save(label);
            log.info(LabelUtils.LABEL_UPDATE_SUCCESS, savedLabel.getId());
            return getLabelDtoFromLabel(savedLabel);
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_UPDATE_ERROR, labelDto.getId(), new LabelNotUpdatedException(ex.getMessage()));
            throw new LabelNotUpdatedException(ex.getMessage());
        }
    }

    public Set<Label> getAllLabelsByUser(Long userId) {
        try {
            Set<Label> labelSet = labelRepository.findAllByUserIdOrderByLabelName(userId);
            return labelSet;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, new LabelsNotFoundException(ex.getMessage()));
            throw new LabelsNotFoundException(ex.getMessage());
        }
    }

    public Set<LabelDto> getLabelsByUser(Long userId) {
        try {
            Set<Label> labelList = getAllLabelsByUser(userId);
            log.info(LabelUtils.LABEL_FETCH_SUCCESS, userId);
            Set<LabelDto> labelDtoSet = labelList.stream().map(label -> getLabelDtoFromLabel(label)).collect(Collectors.toSet());
            return labelDtoSet;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, new LabelsNotFoundException(ex.getMessage()));
            throw new LabelsNotFoundException(ex.getMessage());
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
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, new LabelsNotFoundException(ex.getMessage()));
            throw new LabelsNotFoundException(ex.getMessage());
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
        NoteLabelDto noteLabelDto = new NoteLabelDto();
        try {
            Long labelId = labelIds.stream().findFirst().get();
            Label label = labelRepository.findByIdAndUserId(labelId, userId).orElseThrow(() -> {
                log.error(ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS.getMessage());
                return new NotAuthorizedException();
            });
            if (label.getUserId() == userId) {
                labelRepository.addLabelIdsToNote(labelIds, noteId);
                log.info(LabelUtils.LABEL_PERSIST_SUCCESS);
                Set<Long> noteLabelIds = labelRepository.findLabelIdsByNoteId(noteId);
                log.info(LabelUtils.LABEL_FETCH_SUCCESS);
                noteLabelDto.setNoteId(noteId);
                noteLabelDto.setLabelList(noteLabelIds);
            }
            return noteLabelDto;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_PERSIST_ERROR, new LabelNotPersistedException(ex.getMessage()));
            throw new LabelNotPersistedException(ex.getMessage());
        }
    }

    @Transactional
    public Boolean addLabelInNote(Long labelId, Long noteId, Long userId) {
        try {
            Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundException());
            if (label.getUserId() == userId) {
                labelRepository.addLabelToNote(labelId, noteId);
                log.info(LabelUtils.LABEL_PERSIST_SUCCESS);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_PERSIST_ERROR, new LabelNotPersistedException(ex.getMessage()));
            throw new LabelNotPersistedException(ex.getMessage());
        }
    }

    @Transactional
    public Boolean deleteLabelInNote(Long labelId, Long noteId, Long userId) {
        try {
            Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundException());
            if (label.getUserId() == userId) {
                labelRepository.deleteLabelFromNote(labelId, noteId);
                log.info(LabelUtils.LABEL_DELETE_SUCCESS);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_DELETE_ERROR, new LabelNotDeletedException(ex.getMessage()));
            throw new LabelNotDeletedException(ex.getMessage());
        }
    }


    public Set<Long> getAllNoteIdsWithLabelsByUser(Long userId) {
        try {
            Set<Label> labelSet = getAllLabelsByUser(userId);
            Set<Long> distinctNoteIds = labelSet.stream().flatMap(label -> label.getNoteIds().stream()).collect(Collectors.toSet());
            log.info(LabelUtils.LABEL_FETCH_SUCCESS, userId);
            return distinctNoteIds;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, userId, new LabelsNotFoundException(ex.getMessage()));
            throw new LabelsNotFoundException(ex.getMessage());
        }
    }

    @Transactional
    public Boolean deleteAllLabelsForNote(Long noteId, Long userId) {
        try {
            labelRepository.deleteLabelIdsByNoteId(noteId, userId);
            return true;
        } catch (Exception exp) {
            log.error(LabelUtils.LABEL_DELETE_ERROR);
            throw new LabelNotDeletedException();
        }
    }
}
