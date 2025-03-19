package com.scribblemate.services;

import com.scribblemate.common.event.note.NoteEventData;
import com.scribblemate.common.event.note.NoteLabelEventData;
import com.scribblemate.common.event.note.NoteLabelIdsEventData;
import com.scribblemate.common.utility.EventUtils;
import com.scribblemate.common.utility.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<Long, NoteLabelIdsEventData> labelIdsKafkaTemplate;

    @Autowired
    private KafkaTemplate<Long, NoteLabelEventData> labelKafkaTemplate;

    @Autowired
    private KafkaTemplate<Long, NoteEventData> noteKafkaTemplate;


    public void publishLabelIdsAssignEvent(Set<Long> labelIds, Long id, String email) {
        NoteLabelIdsEventData noteLabelsEventData = setNoteLabelIdsEventData(labelIds, id);
        Message<NoteLabelIdsEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.NOTE_LABELS_ASSIGNED, noteLabelsEventData.getId(), noteLabelsEventData, email);
        log.info("Start - Sending LabelsNoteAssignEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_LABELS_ASSIGNED.getValue(), noteLabelsEventData, email);
        labelIdsKafkaTemplate.send(message);
        log.info("End - Sending LabelsNoteAssignEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_LABELS_ASSIGNED.getValue(), noteLabelsEventData, email);
    }

    public void publishLabelAssignEvent(Long labelId, Long noteId, String email) {
        NoteLabelEventData noteLabelEventData = setNoteLabelEventData(labelId, noteId);
        Message<NoteLabelEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.NOTE_LABEL_ASSIGNED, noteLabelEventData.getId(), noteLabelEventData, email);
        log.info("Start - Sending LabelNoteAssignEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_LABEL_ASSIGNED.getValue(), noteLabelEventData, email);
        labelKafkaTemplate.send(message);
        log.info("End - Sending LabelNoteAssignEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_LABEL_ASSIGNED.getValue(), noteLabelEventData, email);

    }

    public void publishLabelUnassignEvent(Long labelId, Long noteId, String email) {
        NoteLabelEventData noteLabelEventData = setNoteLabelEventData(labelId, noteId);
        Message<NoteLabelEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.NOTE_LABEL_UNASSIGNED, noteLabelEventData.getId(), noteLabelEventData, email);
        log.info("Start - Sending LabelNoteUnassignEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_LABEL_UNASSIGNED.getValue(), noteLabelEventData, email);
        labelKafkaTemplate.send(message);
        log.info("End - Sending LabelNoteUnassignEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_LABEL_UNASSIGNED.getValue(), noteLabelEventData, email);
    }

    public void publishNoteDeletedEvent(Long noteId, String email) {
        NoteEventData noteEventData = setNoteEventData(noteId);
        Message<NoteEventData> message = EventUtils.getMessageWithHeadersAndPayload(Utils.KafkaEvent.NOTE_DELETED, noteEventData.getId(), noteEventData, email);
        log.info("Start - Sending NoteDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_DELETED.getValue(), noteEventData, email);
        noteKafkaTemplate.send(message);
        log.info("End - Sending NoteDeletedEvent {} with headers => {} to Kafka Topic " + Utils.KafkaEvent.NOTE_DELETED.getValue(), noteEventData, email);

    }

    private NoteEventData setNoteEventData(Long noteId) {
        NoteEventData noteEventData = new NoteEventData();
        noteEventData.setId(noteId);
        return noteEventData;
    }

    private NoteLabelEventData setNoteLabelEventData(Long labelId, Long noteId) {
        NoteLabelEventData noteLabelEventData = new NoteLabelEventData();
        noteLabelEventData.setId(noteId);
        noteLabelEventData.setLabelId(labelId);
        return noteLabelEventData;
    }

    private NoteLabelIdsEventData setNoteLabelIdsEventData(Set<Long> labelIds, Long id) {
        NoteLabelIdsEventData noteLabelsEventData = new NoteLabelIdsEventData();
        noteLabelsEventData.setId(id);
        noteLabelsEventData.setLabelIds(labelIds);
        return noteLabelsEventData;
    }
}
