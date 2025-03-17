package com.scribblemate.services;

import com.scribblemate.common.event.label.LabelEventData;
import com.scribblemate.common.exceptions.KafkaEventException;
import com.scribblemate.common.exceptions.LabelsNotFoundException;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.LabelUtils;
import com.scribblemate.entities.Label;
import com.scribblemate.repositories.LabelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    private Label setLabelFromLabelEvent(LabelEventData labelEventData) {
        Label label = new Label();
        label.setId(labelEventData.getId());
        return label;
    }

    @Transactional
    public void persistLabelAfterEvent(LabelEventData labelData) {
        try {
            if (labelRepository.existsById(labelData.getId())) {
                log.error(LabelUtils.LABEL_ALREADY_EXIST_ERROR);
                return;
            }
            Label label = setLabelFromLabelEvent(labelData);
            labelRepository.save(label);
            log.info(EventUtils.LABEL_PERSIST_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.LABEL_PERSIST_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.LABEL_PERSIST_ERROR_EVENT, exp);
        }
    }

    @Transactional
    public void updateLabelAfterEvent(LabelEventData labelData) {
        try {
            Label label = labelRepository.findById(labelData.getId())
                    .orElseThrow(LabelsNotFoundException::new);
            label.setId(labelData.getId());
            labelRepository.save(label);
            log.info(EventUtils.LABEL_UPDATE_SUCCESS_EVENT);
        } catch (LabelsNotFoundException ex) {
            log.error(LabelUtils.LABEL_NOT_FOUND_ERROR);
            return;
        } catch (Exception exp) {
            log.error(EventUtils.LABEL_UPDATE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.LABEL_UPDATE_ERROR_EVENT, exp);
        }
    }

    @Transactional
    public void deleteLabelAfterEvent(LabelEventData labelData) {
        try {
            Label label = labelRepository.findById(labelData.getId())
                    .orElseThrow(LabelsNotFoundException::new);
            labelRepository.deleteLabelsFromLabelNote(label.getId());
            labelRepository.delete(label);
            log.info(EventUtils.LABEL_DELETE_SUCCESS_EVENT);
        } catch (Exception exp) {
            log.error(EventUtils.LABEL_DELETE_ERROR_EVENT);
            throw new KafkaEventException(EventUtils.LABEL_DELETE_ERROR_EVENT, exp);
        }
    }
}
