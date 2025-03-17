package com.scribblemate.services;

import com.scribblemate.client.LabelClient;
import com.scribblemate.common.dto.NoteLabelDto;
import com.scribblemate.common.responses.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FeignService {

    @Autowired
    private LabelClient labelClient;

    public NoteLabelDto addLabelsToNote(Set<Long> labelIds, Long noteId) {
        ResponseEntity<SuccessResponse<NoteLabelDto>> response = labelClient.addLabelListToNote(labelIds, noteId);
        SuccessResponse<NoteLabelDto> successResponse = response.getBody();
        NoteLabelDto noteLabelDto = successResponse.getData();
        return noteLabelDto;
    }

    public Boolean deleteAllLabelsForNote(Long noteId) {
        ResponseEntity<SuccessResponse<Boolean>> response = labelClient.deleteAllLabelsInsideNote(noteId);
        SuccessResponse<Boolean> successResponse = response.getBody();
        Boolean isDeleted = successResponse.getData();
        return isDeleted;
    }

}
