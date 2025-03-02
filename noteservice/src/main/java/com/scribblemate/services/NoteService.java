package com.scribblemate.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.entities.User;
import com.scribblemate.utility.NoteUtils;
import com.scribblemate.common.utility.ResponseErrorUtils;
import com.scribblemate.common.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.dto.CollaboratorDto;
import com.scribblemate.dto.ColorUpdateDto;
import com.scribblemate.dto.ListItemsDto;
import com.scribblemate.dto.NoteDto;
import com.scribblemate.entities.ListItems;
import com.scribblemate.entities.Note;
import com.scribblemate.entities.SpecificNote;
import com.scribblemate.exceptions.labels.LabelNotDeletedException;
import com.scribblemate.exceptions.labels.LabelNotFoundException;
import com.scribblemate.exceptions.notes.CollaboratorAlreadyExistException;
import com.scribblemate.exceptions.notes.CollaboratorDoesNotExistException;
import com.scribblemate.exceptions.notes.CollaboratorNotDeletedException;
import com.scribblemate.exceptions.notes.NoteNotDeletedException;
import com.scribblemate.exceptions.notes.NoteNotFoundException;
import com.scribblemate.exceptions.notes.NoteNotPersistedException;
import com.scribblemate.exceptions.notes.NoteNotUpdatedException;
import com.scribblemate.exceptions.notes.NotesNotFoundException;
import com.scribblemate.repositories.ListItemsRepository;
import com.scribblemate.repositories.NoteRepository;
import com.scribblemate.repositories.SpecificNoteRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoteService {

    @Autowired
    private ListItemsRepository listItemsRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SpecificNoteRepository specificNoteRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public NoteDto createNewNote(NoteDto noteDto, Long userId) {
        try {
            SpecificNote specificNote = new SpecificNote();
            Note savedNote = setSpecificNoteFromNoteDto(noteDto, specificNote, userId);
            log.info(NoteUtils.NOTE_PERSIST_SUCCESS, savedNote);
            return setNoteDtoFromNote(savedNote, userId);
        } catch (Exception ex) {
            log.error(NoteUtils.NOTE_PERSIST_ERROR, new NoteNotPersistedException(ex.getMessage()));
            throw new NoteNotPersistedException(ex.getMessage());
        }
    }

    @Transactional
    public NoteDto updateExistingNote(NoteDto noteDto, Long userId) {
        SpecificNote specificNote = specificNoteRepository.findById(noteDto.getId()).get();
        if (specificNote != null) {
            try {
                Note savedNote = setSpecificNoteFromNoteDto(noteDto, specificNote, userId);
                log.info(NoteUtils.NOTE_UPDATE_SUCCESS, savedNote);
                return setNoteDtoFromNote(savedNote, userId);
            } catch (Exception ex) {
                log.error(NoteUtils.NOTE_UPDATE_ERROR, new NoteNotUpdatedException(ex.getMessage()));
                throw new NoteNotUpdatedException(ex.getMessage());
            }
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteDto.getId());
            throw new NoteNotFoundException();
        }
    }

    @Transactional
    public NoteDto addCollaboratorToNote(Long userId, Long noteId, Long collaboratorId) {
        SpecificNote note = specificNoteRepository.findById(noteId).get();
        if (note != null) {
            // Can possibly make a call to User microservice for User validation

//            User collaborator = userRepository.findByEmail(collaboratorEmail)
//                    .orElseThrow(() -> new CollaboratorDoesNotExistException(
//                            ResponseErrorUtils.COLLABORATOR_DOES_NOT_EXIST_ERROR.getMessage()));

            Note commonNote = note.getCommonNote();
            if (commonNote.getUserIds().stream().anyMatch(id -> id == collaboratorId)) {
                log.error(NoteUtils.COLLABORATOR_ALREADY_EXIST_ERROR);
                throw new CollaboratorAlreadyExistException(
                        ResponseErrorUtils.COLLABORATOR_ALREADY_EXIST_ERROR.getMessage());
            }
            commonNote.getUserIds().add(collaboratorId);
            try {
                SpecificNote specificNote = new SpecificNote();
                specificNote.setCommonNote(commonNote);
                specificNote.setUserId(collaboratorId);
                specificNote.setRole(Utils.Role.COLLABORATOR);
                List<SpecificNote> specificNoteList = commonNote.getSpecificNoteList();
                if (specificNoteList == null) {
                    specificNoteList = new ArrayList<>();
                }
                specificNoteList.add(specificNote);
                commonNote.setSpecificNoteList(specificNoteList);
                Note savedNote = noteRepository.save(commonNote);
                log.info(NoteUtils.NOTE_UPDATE_SUCCESS, savedNote);
                return setNoteDtoFromNote(savedNote, userId);
            } catch (Exception ex) {
                log.error(NoteUtils.NOTE_UPDATE_ERROR, new NoteNotUpdatedException(ex.getMessage()));
                throw new NoteNotUpdatedException(ex.getMessage());
            }

        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }

    @Transactional
    public NoteDto deleteCollaboratorFromNote(Long userId, Long noteId, Long collaboratorId) {
        SpecificNote note = specificNoteRepository.findById(noteId).get();
        if (note != null) {
            try {
                Note commonNote = note.getCommonNote();
                SpecificNote collabNote = specificNoteRepository.findByCommonNoteAndUserId(commonNote, collaboratorId);
                specificNoteRepository.delete(collabNote);
                commonNote.getUserIds().remove(collaboratorId);
                noteRepository.save(commonNote);
                entityManager.flush();
                entityManager.clear();
                log.info(NoteUtils.COLLABORATOR_DELETE_SUCCESS, collaboratorId);
                SpecificNote updatedNote = specificNoteRepository.findById(noteId).get();
                Note updatedCommonNote = updatedNote.getCommonNote();
                return setNoteDtoFromNote(updatedCommonNote, userId);
            } catch (Exception ex) {
                log.error(NoteUtils.COLLABORATOR_DELETE_ERROR, collaboratorId,
                        new CollaboratorNotDeletedException(ex.getMessage()));
                throw new CollaboratorNotDeletedException(ex.getMessage());
            }
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }


    public List<NoteDto> getAllNotesForUser(Long userId) {
        try {
            List<SpecificNote> noteList = specificNoteRepository.findAllByUserIdOrderByCommonNoteCreatedAtDesc(userId);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, userId);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, userId, new NotesNotFoundException(exp.getMessage()));
            throw new NotesNotFoundException(exp.getMessage());

        }
    }

    public List<NoteDto> getAllNotesNonTrashedNonArchivedByUser(Long userId) {
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserIdAndIsTrashedFalseAndIsArchivedFalseOrderByCommonNoteCreatedAtDesc(userId);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, userId);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, userId, new NotesNotFoundException(exp.getMessage()));
            throw new NotesNotFoundException(exp.getMessage());

        }
    }

    public List<NoteDto> getAllNotesByIsTrashed(Long userId) {
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserIdAndIsTrashedTrueOrderByUpdatedAtDesc(userId);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, userId);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, userId, new NoteNotFoundException(exp.getMessage()));
            throw new NoteNotFoundException(exp.getMessage());
        }
    }

    public List<NoteDto> getAllNotesByIsArchived(Long userId) {
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserIdAndIsArchivedTrueOrderByCommonNoteCreatedAtDesc(userId);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, userId);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, userId, new NoteNotFoundException(exp.getMessage()));
            throw new NoteNotFoundException(exp.getMessage());
        }
    }

    public List<NoteDto> getAllNotesByReminder(Long userId) {
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserIdAndReminderNotNullOrderByCommonNoteCreatedAtDesc(userId);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, userId);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, userId, new NoteNotFoundException(exp.getMessage()));
            throw new NoteNotFoundException(exp.getMessage());
        }
    }

    public NoteDto saveNoteAndReturnDto(Long userId, SpecificNote note) {
        try {
            SpecificNote specificNote = specificNoteRepository.save(note);
            Note commonNote = specificNote.getCommonNote();
            log.info(NoteUtils.NOTE_UPDATE_SUCCESS);
            return setNoteDtoFromNote(commonNote, userId);
        } catch (Exception ex) {
            log.error(NoteUtils.NOTE_UPDATE_ERROR, new NoteNotUpdatedException(ex.getMessage()));
            throw new NoteNotUpdatedException(ex.getMessage());
        }
    }

    public NoteDto updateColorOfNote(Long userId, ColorUpdateDto colorDto) {
        SpecificNote note = specificNoteRepository.findById(colorDto.getNoteId()).get();
        if (note != null) {
            note.setColor(colorDto.getColor());
            return saveNoteAndReturnDto(userId, note);
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, colorDto.getNoteId(), new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }

    public NoteDto pinNote(Long userId, Long noteId) {
        SpecificNote note = specificNoteRepository.findById(noteId).get();
        if (note != null) {
            note.setArchived(false);
            note.setTrashed(false);
            note.setPinned(!note.isPinned());
            return saveNoteAndReturnDto(userId, note);
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }

    public NoteDto archiveNote(Long userId, Long noteId) {
        SpecificNote note = specificNoteRepository.findById(noteId).get();
        if (note != null) {
            note.setArchived(!note.isArchived());
            note.setTrashed(false);
            note.setPinned(false);
            return saveNoteAndReturnDto(userId, note);
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }

    public NoteDto trashNote(Long userId, Long noteId) {
        SpecificNote note = specificNoteRepository.findById(noteId).get();
        if (note != null) {
            note.setArchived(false);
            note.setTrashed(!note.isTrashed());
            note.setPinned(false);
            return saveNoteAndReturnDto(userId, note);
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }

    @Transactional
    public boolean deleteNoteByUserAndId(Long userId, Long noteId) {

        SpecificNote note = specificNoteRepository.findById(noteId).get();
        if (note != null) {
            try {
                Note commonNote = note.getCommonNote();
                List<SpecificNote> noteList = specificNoteRepository.findAllByCommonNote(commonNote);
                log.info(NoteUtils.NOTE_DELETE_SUCCESS);
                if (noteList.size() == 1) {
                    noteRepository.deleteNoteImages(commonNote.getId());
                    noteRepository.deleteListItems(commonNote.getId());
                    noteRepository.deleteById(commonNote.getId());
                    log.info(NoteUtils.NOTE_PERMANENT_DELETE_SUCCESS);
                }
                specificNoteRepository.deleteByIdAndUserId(noteId, userId);
                return true;
            } catch (Exception ex) {
                log.error(NoteUtils.ERROR_DELETING_NOTE_FOR_USER, new NoteNotDeletedException(ex.getMessage()));
                throw new NoteNotDeletedException(ex.getMessage());
            }
        } else {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
    }

    @Transactional
    public void insertListItemAtPosition(Note note, ListItems newItem, int position) {
        List<ListItems> listItems = note.getListItems();
        for (int i = position; i < listItems.size(); i++) {
            ListItems item = listItems.get(i);
            item.setOrderIndex(item.getOrderIndex() + 1);
        }
        newItem.setOrderIndex(position);
        listItems.add(position, newItem);
        listItemsRepository.saveAll(listItems);
        listItemsRepository.save(newItem);
    }

    @Transactional
    public void removeListItem(Note note, ListItems itemToRemove) {
        List<ListItems> listItems = note.getListItems();
        int removedIndex = itemToRemove.getOrderIndex();
        listItems.remove(itemToRemove);
        for (ListItems item : listItems) {
            if (item.getOrderIndex() > removedIndex) {
                item.setOrderIndex(item.getOrderIndex() - 1);
            }
        }
        listItemsRepository.saveAll(listItems);
    }

    private List<NoteDto> getNoteDtoFromNoteList(List<SpecificNote> noteList, Long userId) {
        List<NoteDto> noteDtoList = noteList.stream().map(specificNote -> {
            Note note = specificNote.getCommonNote();
            return setNoteDtoFromNote(note, userId);
        }).collect(Collectors.toList());
        log.info(NoteUtils.NOTE_FETCH_SUCCESS, userId);
        return noteDtoList;
    }

    private NoteDto setNoteDtoFromNote(Note note, Long userId) {
        NoteDto noteDto = new NoteDto();
        noteDto.setTitle(note.getTitle());
        noteDto.setContent(note.getContent());
        setNoteDtoListItemsFromNoteListItems(noteDto, note.getListItems());
        noteDto.setImages(note.getImages());
        noteDto.setCreatedBy(note.getCreatedBy());
        noteDto.setCollaboratorIds(note.getUserIds());
        List<SpecificNote> specificNoteList = note.getSpecificNoteList();
        SpecificNote specificNote = specificNoteList.stream().filter(noteItem -> userId == noteItem.getUserId())
                .findFirst().get();
        noteDto.setId(specificNote.getId());
        noteDto.setColor(specificNote.getColor());
        noteDto.setCreatedAt(specificNote.getCreatedAt());
        if (note.getUpdatedAt() != null && note.getCreatedAt().compareTo(note.getUpdatedAt()) != 0) {
            noteDto.setUpdatedAt(note.getUpdatedAt());
            noteDto.setUpdatedBy(note.getUpdatedBy());
        }
        noteDto.setArchived(specificNote.isArchived());
        noteDto.setPinned(specificNote.isPinned());
        noteDto.setReminder(specificNote.getReminder());
        noteDto.setTrashed(specificNote.isTrashed());
        log.info(NoteUtils.NOTE_CREATED_AND_RETURN, noteDto);
        return noteDto;
    }

    private void setNoteDtoListItemsFromNoteListItems(NoteDto noteDto, List<ListItems> listItems) {
        List<ListItemsDto> dtoListItems = noteDto.getListItems();
        if (listItems != null) {
            listItems.forEach(item -> {
                ListItemsDto itemDto = new ListItemsDto();
                itemDto.setContent(item.getContent());
                itemDto.setDone(item.isDone());
                itemDto.setId(item.getId());
                itemDto.setOrderIndex(item.getOrderIndex());
                itemDto.setCreatedAt(item.getCreatedAt());
                itemDto.setUpdatedAt(item.getUpdatedAt());
                dtoListItems.add(itemDto);
            });
        }
    }

    @Transactional
    private Note setNoteFromNoteDto(NoteDto noteDto, Note note, Long userId) {
        if (note.getId() == null) {
            note.setCreatedBy(userId);
        }
        note.setUpdatedBy(userId);
        note.setTitle(noteDto.getTitle());
        note.setContent(noteDto.getContent());
        setNoteListItemsFromNoteDtoListItems(noteDto.getListItems(), note);
        note.setImages(noteDto.getImages());
        Note mappedNote = noteRepository.save(note);
        if (noteDto.getCollaboratorIds() != null) {
            Set<Long> collabIdSet = noteDto.getCollaboratorIds().stream()
                    .filter(id -> id != userId).collect(Collectors.toSet());
            Set<Long> nonExistingIds = collabIdSet.stream().filter(
                            id -> specificNoteRepository.findByCommonNoteAndUserId(mappedNote, id) == null)
                    .collect(Collectors.toSet());
            nonExistingIds.stream().forEach(id -> {
                SpecificNote specificNote = new SpecificNote();
                specificNote.setCommonNote(mappedNote);
                specificNote.setUserId(id);
                specificNote.setRole(Utils.Role.COLLABORATOR);
                specificNoteRepository.save(specificNote);
            });
            mappedNote.getUserIds().addAll(nonExistingIds);
            mappedNote.getUserIds().add(userId);
        }
        Note savedNote = noteRepository.save(mappedNote);
        return savedNote;
    }

    private void setNoteListItemsFromNoteDtoListItems(List<ListItemsDto> noteDtoListItems, Note note) {
        List<ListItems> listItems = note.getListItems();
        if (noteDtoListItems != null) {
            noteDtoListItems.forEach(itemDto -> {
                ListItems item = new ListItems();
                item.setContent(itemDto.getContent());
                item.setDone(itemDto.isDone());
                item.setOrderIndex(itemDto.getOrderIndex());
                item.setId(itemDto.getId());
                item.setCreatedAt(itemDto.getCreatedAt());
                item.setUpdatedAt(itemDto.getUpdatedAt());
                listItems.add(item);
            });
        }

    }

    @Transactional
    private Note setSpecificNoteFromNoteDto(NoteDto noteDto, SpecificNote specificNote, Long userId) {
        Note note = specificNote.getCommonNote();
        if (note == null) {
            note = new Note();
        }
        Note updatedNote = setNoteFromNoteDto(noteDto, note, userId);
        specificNote.setColor(noteDto.getColor());
        specificNote.setArchived(noteDto.isArchived());
        specificNote.setUpdatedAt(noteDto.getUpdatedAt());
        specificNote.setCreatedAt(noteDto.getCreatedAt());
        if (noteDto.getLabelSet() != null) {
            List<Long> labelIds = noteDto.getLabelSet().stream().collect(Collectors.toList());
            Iterable<Long> iterableIds = labelIds;
            // Event publish on labelService side when adding , updating , deleting User Labels which noteService will consume
            // If not Event then feign client call to the Label Service to add Labels to a Note


//            List<Label> labelList = labelRepository.findAllById(iterableIds).stream()
//                    .filter(label -> label.getUser().getEmail().equals(user.getEmail())).toList();
//            if (!labelList.isEmpty()) {
//                labelList.stream().forEach(label -> {
//                    if (label.getNoteList() != null) {
//                        label.getNoteList().add(specificNote);
//                    } else {
//                        List<SpecificNote> noteList = new ArrayList<>();
//                        noteList.add(specificNote);
//                        label.setNoteList(noteList);
//                    }
//                });
//                if (specificNote.getLabelSet() == null) {
//                    Set<Label> labelSet = new HashSet<>();
//                    labelSet.addAll(labelList);
//                    specificNote.setLabelSet(labelSet);
//                } else {
//                    specificNote.getLabelSet().addAll(labelList);
//                }
//            }

        }
        if (specificNote.getRole() == null) {
            specificNote.setRole(Utils.Role.OWNER);
        }
        specificNote.setPinned(noteDto.isPinned());
        specificNote.setReminder(noteDto.getReminder());
        specificNote.setTrashed(noteDto.isTrashed());
        specificNote.setUserId(userId);
        specificNote.setCommonNote(updatedNote);
        SpecificNote savedSpecificNote = specificNoteRepository.save(specificNote);
        List<SpecificNote> specificNoteList = updatedNote.getSpecificNoteList();
        if (specificNoteList == null) {
            specificNoteList = new ArrayList<>();
            updatedNote.setSpecificNoteList(specificNoteList);
        }
        specificNoteList.add(savedSpecificNote);
        return updatedNote;
    }

    public NoteDto getNoteById(Long userId, Long id) {
        SpecificNote specificNote = specificNoteRepository.findById(id).get();
        Note note = specificNote.getCommonNote();
        NoteDto noteDto = setNoteDtoFromNote(note, userId);
        return noteDto;
    }

}
