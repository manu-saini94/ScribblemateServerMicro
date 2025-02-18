package com.scribblemate.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.scribblemate.exceptions.UserNotFoundException;
import com.scribblemate.exceptions.labels.*;
import com.scribblemate.repositories.SpecificNoteRepository;
import com.scribblemate.utility.LabelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.dto.LabelDto;
import com.scribblemate.entities.Label;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.LabelRepository;
import com.scribblemate.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LabelService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private SpecificNoteRepository specificNoteRepository;

    @Transactional
    public LabelDto createNewLabel(LabelDto labelDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        try {
            Set<Label> labelSet = user.getLabelSet();
            Label label = new Label();
            label.setLabelName(labelDto.getLabelName());
            label.setImportant(labelDto.isImportant());
            label.setUser(user);
            if (labelSet == null) {
                labelSet = new HashSet<>();
                labelSet.add(label);
                user.setLabelSet(labelSet);
            } else {
                boolean alreadyExist = labelSet.stream()
                        .anyMatch(labelItem -> labelItem.getLabelName().equalsIgnoreCase(labelDto.getLabelName()));
                if (alreadyExist) {
                    log.error(LabelUtils.LABEL_ALREADY_EXIST_ERROR, label);
                    throw new LabelAlreadyExistException();
                }
                labelSet.add(label);
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
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        try {
            specificNoteRepository.deleteLabelsFromLabelNote(labelId);
            labelRepository.deleteByIdAndUser(labelId, user.getId());
            log.info(LabelUtils.LABEL_DELETE_SUCCESS, labelId);
            return true;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_DELETE_ERROR, labelId, new LabelNotDeletedException(ex.getMessage()));
            throw new LabelNotDeletedException(ex.getMessage());
        }
    }

    @Transactional
    public LabelDto editLabel(LabelDto labelDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        try {
            Label label = labelRepository.findByIdAndUser(labelDto.getId(), user);
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
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());

        try {
            List<Label> labelList = labelRepository.findAllByUserOrderByLabelName(user);
            List<LabelDto> labelDtoList = labelList.stream().map(label -> {
                return getLabelDtoFromLabel(label);
            }).toList();
            log.info(LabelUtils.LABEL_FETCH_SUCCESS, user.getId());
            return labelDtoList;
        } catch (Exception ex) {
            log.error(LabelUtils.LABEL_FETCH_ERROR, user.getId(), new LabelsNotFoundException(ex.getMessage()));
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
