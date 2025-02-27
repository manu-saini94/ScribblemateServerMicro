package com.scribblemate.services;

import java.util.List;
import com.scribblemate.exceptions.labels.*;
import com.scribblemate.repositories.SpecificNoteRepository;
import com.scribblemate.utility.LabelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.dto.LabelDto;
import com.scribblemate.entities.Label;
import com.scribblemate.repositories.LabelRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private SpecificNoteRepository specificNoteRepository;

    @Transactional
    public LabelDto createNewLabel(LabelDto labelDto, Long userId) {
        try {
            Label label = new Label();
            label.setLabelName(labelDto.getLabelName());
            label.setImportant(labelDto.isImportant());
            label.setUserId(userId);
            Label foundLabel = labelRepository.findByUserIdAndLabelName(userId,labelDto.getLabelName());
                if (foundLabel!=null) {
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
            specificNoteRepository.deleteLabelsFromLabelNote(labelId);
            labelRepository.deleteByIdAndUser(labelId, userId);
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
            Label label = labelRepository.findByUserIdAndLabelName(userId,labelDto.getLabelName());
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

    @Transactional
    public List<LabelDto> getLabelsByUser(Long userId) {
        try {
            List<Label> labelList = labelRepository.findAllByUserIdOrderByLabelName(userId);
            List<LabelDto> labelDtoList = labelList.stream().map(label -> getLabelDtoFromLabel(label)).toList();
            log.info(LabelUtils.LABEL_FETCH_SUCCESS,userId);
            return labelDtoList;
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
        labelDto.setCreatedAt(savedLabel.getCreatedAt());
        if (savedLabel.getCreatedAt().compareTo(savedLabel.getUpdatedAt()) != 0) {
            labelDto.setUpdatedAt(savedLabel.getUpdatedAt());
        }
        return labelDto;
    }

}
