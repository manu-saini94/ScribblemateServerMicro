package com.scribblemate.services;

import com.scribblemate.common.event.label.LabelEventData;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.Utils;
import com.scribblemate.entities.Label;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<Long, LabelEventData> kafkaTemplate;

    public void publishLabelCreatedEvent(Label label, String userEmail) {
        LabelEventData labelCreatedData = setLabelEventDataFromLabel(label);
        Message<LabelEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.LABEL_CREATED, labelCreatedData.getId(), labelCreatedData, userEmail);
        log.info("Start - Sending LabelCreatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.LABEL_CREATED.getValue(), labelCreatedData, message);
        kafkaTemplate.send(message);
        log.info("End - Sending LabelCreatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.LABEL_CREATED.getValue(), labelCreatedData, message);
    }

    public void publishLabelUpdatedEvent(Label label, String userEmail) {
        LabelEventData labelUpdatedData = setLabelEventDataFromLabel(label);
        Message<LabelEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.LABEL_UPDATED, labelUpdatedData.getId(), labelUpdatedData, userEmail);
        log.info("Start - Sending LabelUpdatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.LABEL_UPDATED.getValue(), labelUpdatedData, message);
        kafkaTemplate.send(message);
        log.info("End - Sending LabelUpdatedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.LABEL_UPDATED.getValue(), labelUpdatedData, message);
    }

    public void publishLabelDeletedEvent(Label label, String userEmail) {
        LabelEventData labelDeletedData = setLabelEventDataFromLabel(label);
        Message<LabelEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.LABEL_DELETED, labelDeletedData.getId(), labelDeletedData, userEmail);
        log.info("Start - Sending LabelDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.LABEL_DELETED.getValue(), labelDeletedData, message);
        kafkaTemplate.send(message);
        log.info("End - Sending LabelDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.LABEL_DELETED.getValue(), labelDeletedData, message);
    }

    private LabelEventData setLabelEventDataFromLabel(Label label) {
        LabelEventData labelEventData = new LabelEventData();
        labelEventData.setId(label.getId());
        labelEventData.setLabelName(label.getLabelName());
        labelEventData.setImportant(label.isImportant());
        labelEventData.setCreatedAt(label.getCreatedAt());
        labelEventData.setUpdatedAt(label.getUpdatedAt());
        return labelEventData;
    }
}
