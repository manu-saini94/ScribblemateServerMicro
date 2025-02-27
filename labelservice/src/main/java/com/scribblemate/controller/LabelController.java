package com.scribblemate.controller;

import java.util.List;

import com.scribblemate.annotation.LoadUserContext;
import com.scribblemate.configuration.UserContext;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import com.scribblemate.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.scribblemate.dto.LabelDto;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.services.LabelService;

@RequestMapping("${labels.api.prefix}")
@RestController
@LoadUserContext
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<LabelDto>> createLabel(@RequestBody LabelDto labelDto) {
        User user = UserContext.getCurrentUser();
        LabelDto newLabelDto = labelService.createNewLabel(labelDto, user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_PERSIST_SUCCESS, newLabelDto));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse<LabelDto>> updateLabel(@RequestBody LabelDto labelDto) {
        User user = UserContext.getCurrentUser();
        LabelDto updatedLabelDto = labelService.editLabel(labelDto, user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_UPDATE_SUCCESS, updatedLabelDto));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<List<LabelDto>>> getAllLabelsByUser() {
        User user = UserContext.getCurrentUser();
        List<LabelDto> labelList = labelService.getLabelsByUser(user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_FETCHING_SUCCESS, labelList));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse<Boolean>> deleteLabelByUser(@RequestParam("id") Long labelId) {
        User user = UserContext.getCurrentUser();
        boolean isDeleted = labelService.deleteLabel(labelId, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_DELETE_SUCCESS, isDeleted));
    }

}
