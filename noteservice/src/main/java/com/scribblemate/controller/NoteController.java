package com.scribblemate.controller;

import java.util.List;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import com.scribblemate.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scribblemate.dto.ColorUpdateDto;
import com.scribblemate.dto.NoteDto;
import com.scribblemate.services.NoteService;

@RequestMapping("${notes.api.prefix}")
@RestController
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<NoteDto>> createNote(@RequestBody NoteDto notedto,
                                                               @AuthenticationPrincipal User user) {
        NoteDto note = noteService.createNewNote(notedto, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_PERSIST_SUCCESS, note));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllNotes(@AuthenticationPrincipal User user) {
        List<NoteDto> notesList = noteService.getAllNotesForUser(user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/get")
    public ResponseEntity<SuccessResponse<NoteDto>> getNote(@RequestParam("noteId") Long noteId,
                                                            @AuthenticationPrincipal User user) {
        NoteDto note = noteService.getNoteById(user.getId(), noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, note));
    }

    @GetMapping("/get/trash")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllTrashedNotes(@AuthenticationPrincipal User user) {
        List<NoteDto> notesList = noteService.getAllNotesByIsTrashed(user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/get/archive")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllArchivedNotes(@AuthenticationPrincipal User user) {
        List<NoteDto> notesList = noteService.getAllNotesByIsArchived(user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/get/reminder")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllReminderNotes(@AuthenticationPrincipal User user) {
        List<NoteDto> notesList = noteService.getAllNotesByReminder(user.getId());
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse<NoteDto>> updateNote(@RequestBody NoteDto notedto,
                                                               @AuthenticationPrincipal User user) {
        NoteDto note = noteService.updateExistingNote(notedto, user.getId());
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/pin")
    public ResponseEntity<SuccessResponse> pinNote(@RequestParam("noteId") Long noteId,
                                                   @AuthenticationPrincipal User user) {
        NoteDto note = noteService.pinNote(user.getId(), noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/color")
    public ResponseEntity<SuccessResponse> updateColor(@RequestBody ColorUpdateDto colorDto,
                                                       @AuthenticationPrincipal User user) {
        NoteDto note = noteService.updateColorOfNote(user.getId(), colorDto);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/archive")
    public ResponseEntity<SuccessResponse> archiveNote(@RequestParam("noteId") Long noteId,
                                                       @AuthenticationPrincipal User user) {
        NoteDto note = noteService.archiveNote(user.getId(), noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/trash")
    public ResponseEntity<SuccessResponse> trashNote(@RequestParam("noteId") Long noteId,
                                                     @AuthenticationPrincipal User user) {
        NoteDto note = noteService.trashNote(user.getId(), noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PostMapping("/add/collaborator")
    public ResponseEntity<SuccessResponse> addCollaboratorToNote(@RequestParam("collaboratorId") Long collaboratorId,
                                                                 @RequestParam("noteId") Long noteId,
                                                                 @AuthenticationPrincipal User user) {
        NoteDto note = noteService.addCollaboratorToNote(user.getId(), noteId, collaboratorId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @DeleteMapping("/delete/collaborator")
    public ResponseEntity<SuccessResponse> removeCollaboratorFromNote(@RequestParam("noteId") Long noteId,
                                                                      @RequestParam("collaboratorId") Long collaboratorId,
                                                                      @AuthenticationPrincipal User user) {
        NoteDto note = noteService.deleteCollaboratorFromNote(user.getId(), noteId, collaboratorId);
        return ResponseEntity.ok().body(
                new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.COLLABORATOR_DELETE_SUCCESS, note));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse> deleteNoteByUser(@RequestParam("noteId") Long noteId,
                                                            @AuthenticationPrincipal User user) {
        boolean isDeleted = noteService.deleteNoteByUserAndId(user.getId(), noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(),
                        ResponseSuccessUtils.NOTE_DELETE_SUCCESS, isDeleted));
    }

}
