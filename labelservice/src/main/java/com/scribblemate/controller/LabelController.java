package com.scribblemate.controller;

import java.util.List;

import com.scribblemate.utility.ResponseSuccessUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scribblemate.dto.LabelDto;
import com.scribblemate.responses.SuccessResponse;
import com.scribblemate.services.LabelService;

@RequestMapping("${labels.api.prefix}")
@RestController
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse> createLabel(@RequestBody LabelDto labelDto, @RequestHeader("userId") Long userId) {
        LabelDto newLabelDto = labelService.createNewLabel(labelDto, userId);
        return ResponseEntity.ok().body(
                new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_PERSIST_SUCCESS, newLabelDto));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse> updateLabel(@RequestBody LabelDto labelDto, @RequestHeader("userId") Long userId) {
        LabelDto updatedLabelDto = labelService.editLabel(labelDto, userId);
        return ResponseEntity.ok().body(
                new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_UPDATE_SUCCESS, updatedLabelDto));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse> getAllLabelsByUser(@RequestHeader("userId") Long userId) {
        List<LabelDto> labelList = labelService.getLabelsByUser(userId);
        return ResponseEntity.ok().body(
                new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_FETCHING_SUCCESS, labelList));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse> deleteLabelByUser(@RequestParam("id") Long labelId,
                                                             @RequestHeader("userId") Long userId) {
        boolean isDeleted = labelService.deleteLabel(labelId, userId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.LABEL_DELETE_SUCCESS, isDeleted));
    }

}
