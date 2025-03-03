package com.scribblemate.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.scribblemate.common.dto.NoteLabelDto;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import com.scribblemate.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.scribblemate.dto.LabelDto;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.services.LabelService;

@RequestMapping("${labels.api.prefix}")
@RestController
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class LabelController {

    @Autowired
    private LabelService labelService;

    // Create Api for getting all notes with labels

//    @GetMapping("/labelled")
//    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllNotesWithLabels(@AuthenticationPrincipal User user) {
//        List<NoteDto> notesList = noteService.getAllNotesWithLabelsByUser(user);
//        return ResponseEntity.ok().body(
//                new SuccessResponse<>(HttpStatus.OK.value(),
//                        ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
//    }

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<LabelDto>> createLabel(@RequestBody LabelDto labelDto,
                                                                 @AuthenticationPrincipal User user) {
        LabelDto newLabelDto = labelService.createNewLabel(labelDto, user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_PERSIST_SUCCESS, newLabelDto));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse<LabelDto>> updateLabel(@RequestBody LabelDto labelDto,
                                                                 @AuthenticationPrincipal User user) {
        LabelDto updatedLabelDto = labelService.editLabel(labelDto, user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_UPDATE_SUCCESS, updatedLabelDto));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<Set<LabelDto>>> getAllLabelsByUser(@AuthenticationPrincipal User user) {
        Set<LabelDto> labelList = labelService.getLabelsByUser(user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_FETCHING_SUCCESS, labelList));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse<Boolean>> deleteLabelByUser(@RequestParam("id") Long labelId,
                                                                      @AuthenticationPrincipal User user) {
        boolean isDeleted = labelService.deleteLabel(labelId, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_DELETE_SUCCESS, isDeleted));
    }

    @GetMapping("/note/all")
    public ResponseEntity<SuccessResponse<Map<Long, Set<Long>>>> getLabelsWithNoteIds(@AuthenticationPrincipal User user) {
        Map<Long, Set<Long>> labelsByNotesMap = labelService.getLabelsByNoteIds(user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_FETCHING_SUCCESS, labelsByNotesMap));
    }

    @PutMapping("/note/{noteId}/assign")
    public ResponseEntity<SuccessResponse<NoteLabelDto>> addLabelListToNote(@RequestBody Set<Long> labelIds,
                                                                            @PathVariable("noteId") Long noteId,
                                                                            @AuthenticationPrincipal User user) {
        NoteLabelDto noteLabelDto = labelService.addLabelListInNote(labelIds, noteId, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_UPDATE_SUCCESS, noteLabelDto));

    }

    @PutMapping("/note/{noteId}/assign/{labelId}")
    public ResponseEntity<SuccessResponse<Boolean>> addLabelToNote(@PathVariable("labelId") Long labelId,
                                                                   @PathVariable("noteId") Long noteId,
                                                                   @AuthenticationPrincipal User user) {
        Boolean isAdded = labelService.addLabelInNote(labelId, noteId, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_UPDATE_SUCCESS, isAdded));

    }

    @DeleteMapping("/note/{noteId}/unassign/{labelId}")
    public ResponseEntity<SuccessResponse<Boolean>> deleteLabelInsideNote(@PathVariable("labelId") Long labelId,
                                                                          @PathVariable("noteId") Long noteId,
                                                                          @AuthenticationPrincipal User user) {
        Boolean isAdded = labelService.deleteLabelInNote(labelId, noteId, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.LABEL_DELETE_SUCCESS, isAdded));
    }
}
