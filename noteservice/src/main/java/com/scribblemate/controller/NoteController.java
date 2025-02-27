package com.scribblemate.controller;

import java.util.List;
import java.util.Map;
import com.scribblemate.configuration.UserContext;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.scribblemate.entities.User;
import com.scribblemate.services.NoteService;
import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("${notes.api.prefix}")
@RestController
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<NoteDto>> createNote(@RequestBody NoteDto notedto) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.createNewNote(notedto, user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_PERSIST_SUCCESS, note));
    }

    @PostMapping("/add/label")
    public ResponseEntity<SuccessResponse<NoteDto>> addLabelToNote(@RequestParam("labelId") Long labelId,
                                                          @RequestParam("noteId") Long noteId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.addLabelToNote(user, noteId, labelId);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @DeleteMapping("/delete/label")
    public ResponseEntity<SuccessResponse<NoteDto>> removeLabelFromNote(@RequestParam("noteId") Long noteId,
                                                               @RequestParam("labelId") Long labelId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.deleteLabelFromNote(user, noteId, labelId);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.COLLABORATOR_DELETE_SUCCESS, note));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllNotes() {
        User user = UserContext.getCurrentUser();
        List<NoteDto> notesList = noteService.getAllNotesForUser(user);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/label")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getNotesByLabel(@RequestParam("labelId") Long labelId) {
        User user = UserContext.getCurrentUser();
        List<NoteDto> notesList = noteService.getNotesByUserAndLabelId(user, labelId);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/label/all")
    public ResponseEntity<SuccessResponse<Map<Long, List<Long>>>> getAllNotesByLabelIds() {
        User user = UserContext.getCurrentUser();
        Map<Long, List<Long>> notesMap = noteService.getAllNotesByUserAndLabelIds(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesMap));
    }

    @GetMapping("/labelled")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllNotesWithLabels() {
        User user = UserContext.getCurrentUser();
        List<NoteDto> notesList = noteService.getAllNotesWithLabelsByUser(user);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/get")
    public ResponseEntity<SuccessResponse<NoteDto>> getNote(@RequestParam("noteId") Long noteId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.getNoteById(user, noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, note));
    }

    @GetMapping("/get/trash")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllTrashedNotes() {
        User user = UserContext.getCurrentUser();
        List<NoteDto> notesList = noteService.getAllNotesByIsTrashed(user);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/get/archive")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllArchivedNotes() {
        User user = UserContext.getCurrentUser();
        List<NoteDto> notesList = noteService.getAllNotesByIsArchived(user);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @GetMapping("/get/reminder")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllReminderNotes() {
        User user = UserContext.getCurrentUser();
        List<NoteDto> notesList = noteService.getAllNotesByReminder(user);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_FETCHING_SUCCESS, notesList));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse<NoteDto>> updateNote(@RequestBody NoteDto notedto) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.updateExistingNote(notedto, user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/pin")
    public ResponseEntity<SuccessResponse> pinNote(@RequestParam("noteId") Long noteId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.pinNote(user, noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/color")
    public ResponseEntity<SuccessResponse> updateColor(@RequestBody ColorUpdateDto colorDto) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.updateColorOfNote(user, colorDto);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/archive")
    public ResponseEntity<SuccessResponse> archiveNote(@RequestParam("noteId") Long noteId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.archiveNote(user, noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PutMapping("/update/trash")
    public ResponseEntity<SuccessResponse> trashNote(@RequestParam("noteId") Long noteId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.trashNote(user, noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @PostMapping("/add/collaborator")
    public ResponseEntity<SuccessResponse> addCollaboratorToNote(
            @RequestParam("collaboratorEmail") String collaboratorEmail, @RequestParam("noteId") Long noteId) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.addCollaboratorToNote(user, noteId, collaboratorEmail);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_UPDATE_SUCCESS, note));
    }

    @DeleteMapping("/delete/collaborator")
    public ResponseEntity<SuccessResponse> removeCollaboratorFromNote(@RequestParam("noteId") Long noteId,
                                                                      @RequestParam("collaboratorEmail") String collaboratorEmail) {
        User user = UserContext.getCurrentUser();
        NoteDto note = noteService.deleteCollaboratorFromNote(user, noteId, collaboratorEmail);
        return ResponseEntity.ok().body(
                new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.COLLABORATOR_DELETE_SUCCESS, note));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse> deleteNoteByUser(@RequestParam("noteId") Long noteId) {

        User user = UserContext.getCurrentUser();
        boolean isDeleted = noteService.deleteNoteByUserAndId(user, noteId);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.NOTE_DELETE_SUCCESS, isDeleted));
    }

}
