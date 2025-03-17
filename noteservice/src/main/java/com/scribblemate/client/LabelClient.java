package com.scribblemate.client;

import com.scribblemate.common.dto.NoteLabelDto;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.configuration.FeignClientConfiguration;
import com.scribblemate.entities.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@Component
@FeignClient(name = "label-service",
        url = "${labels.server.url}${labels.api.prefix}",
        configuration = FeignClientConfiguration.class)
public interface LabelClient {

    @PutMapping("/note/{noteId}/assign")
    public ResponseEntity<SuccessResponse<NoteLabelDto>> addLabelListToNote(@RequestBody Set<Long> labelIds,
                                                                            @PathVariable("noteId") Long noteId);

    @DeleteMapping("/note/{noteId}/unassign")
    public ResponseEntity<SuccessResponse<Boolean>> deleteAllLabelsInsideNote(@PathVariable("noteId") Long noteId);
}
