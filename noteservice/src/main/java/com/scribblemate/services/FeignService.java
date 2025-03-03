package com.scribblemate.services;

import com.scribblemate.client.LabelClient;
import com.scribblemate.common.dto.CollaboratorDto;
import com.scribblemate.common.dto.NoteLabelDto;
import com.scribblemate.common.responses.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeignService {

    @Autowired
    private LabelClient labelClient;

    public NoteLabelDto addLabelsToNote(List<Long> labelIds,Long noteId){
        ResponseEntity<SuccessResponse<NoteLabelDto>> response = labelClient.addLabelListToNote(labelIds,noteId);
        SuccessResponse<NoteLabelDto> successResponse = response.getBody();
        NoteLabelDto noteLabelDto = successResponse.getData();
        return noteLabelDto;
    }

}
