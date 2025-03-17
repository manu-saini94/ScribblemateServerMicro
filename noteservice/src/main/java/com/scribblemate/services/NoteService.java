package com.scribblemate.services;

import java.util.*;
import java.util.stream.Collectors;

import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.utility.UserUtils;
import com.scribblemate.common.dto.CollaboratorDto;
import com.scribblemate.entities.*;
import com.scribblemate.exceptions.labels.LabelNotDeletedException;
import com.scribblemate.exceptions.labels.LabelNotFoundException;
import com.scribblemate.exceptions.notes.*;
import com.scribblemate.repositories.*;
import com.scribblemate.utility.NoteUtils;
import com.scribblemate.common.utility.ResponseErrorUtils;
import com.scribblemate.common.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.dto.ColorUpdateDto;
import com.scribblemate.dto.ListItemsDto;
import com.scribblemate.dto.NoteDto;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NoteService {

    @Autowired
    private ListItemsRepository listItemsRepository;

    @Autowired
    private KafkaListenerService kafkaListenerService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SpecificNoteRepository specificNoteRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Transactional
    public NoteDto createNewNote(NoteDto noteDto, Long userId) {
        User user = checkUserAndReturn(userId);
        try {
            SpecificNote specificNote = new SpecificNote();
            Note savedNote = setSpecificNoteFromNoteDto(noteDto, specificNote, user);
            log.info(NoteUtils.NOTE_PERSIST_SUCCESS, savedNote);
            return setNoteDtoFromNote(savedNote, user);
        } catch (Exception ex) {
            log.error(NoteUtils.NOTE_PERSIST_ERROR, noteDto, ex.getMessage());
            throw new NoteNotPersistedException(ex.getMessage());
        }
    }

    @Transactional
    public NoteDto updateExistingNote(NoteDto noteDto, Long userId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteDto.getId());
        try {
            Note savedNote = setSpecificNoteFromNoteDto(noteDto, specificNote, user);
            log.info(NoteUtils.NOTE_UPDATE_SUCCESS, savedNote);
            return setNoteDtoFromNote(savedNote, user);
        } catch (Exception ex) {
            log.error(NoteUtils.NOTE_UPDATE_ERROR, noteDto.getId(), ex.getMessage());
            throw new NoteNotUpdatedException(ex);
        }
    }


    @Transactional
    public NoteDto addCollaboratorToNote(Long userId, Long noteId, String collaboratorEmail) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        User collaborator = userRepository.findByEmail(collaboratorEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        ResponseErrorUtils.USER_NOT_FOUND.getMessage()));
        Note commonNote = specificNote.getCommonNote();
        List<User> collaboratorList = commonNote.getCollaboratorList();
        Boolean isExist = collaboratorList.stream().anyMatch(collab -> collab.getEmail().equals(collaboratorEmail));
        if (isExist) {
            log.error(NoteUtils.COLLABORATOR_ALREADY_EXIST_ERROR, collaboratorEmail);
            throw new CollaboratorAlreadyExistException(
                    ResponseErrorUtils.COLLABORATOR_ALREADY_EXIST_ERROR.getMessage());
        } else {
            commonNote.getCollaboratorList().add(collaborator);
        }
        try {
            SpecificNote newSpecificNote = new SpecificNote();
            newSpecificNote.setCommonNote(commonNote);
            newSpecificNote.setUser(collaborator);
            newSpecificNote.setRole(Utils.Role.COLLABORATOR);
            List<SpecificNote> specificNoteList = commonNote.getSpecificNoteList();
            specificNoteList.add(newSpecificNote);
            commonNote.setSpecificNoteList(specificNoteList);
            Note savedNote = noteRepository.save(commonNote);
            log.info(NoteUtils.NOTE_UPDATE_SUCCESS, savedNote);
            return setNoteDtoFromNote(savedNote, user);
        } catch (Exception ex) {
            log.error(NoteUtils.COLLABORATOR_ADD_ERROR, collaboratorEmail, ex.getMessage());
            throw new NoteNotUpdatedException(ex);
        }

    }

    @Transactional
    public NoteDto deleteCollaboratorFromNote(Long userId, Long noteId, String collaboratorEmail) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        try {
            Note commonNote = specificNote.getCommonNote();
            User collaborator = userRepository.findByEmail(collaboratorEmail)
                    .orElseThrow(() -> new CollaboratorDoesNotExistException(
                            ResponseErrorUtils.COLLABORATOR_DOES_NOT_EXIST_ERROR.getMessage()));
            SpecificNote collabNote = specificNoteRepository.findByCommonNoteAndUser(commonNote, collaborator);
            if (collabNote != null) {
                specificNoteRepository.delete(collabNote);
                commonNote.getSpecificNoteList().remove(collabNote);
                commonNote.getCollaboratorList().remove(collaborator);
                noteRepository.save(commonNote);
                entityManager.flush();
                entityManager.clear();
                log.info(NoteUtils.COLLABORATOR_DELETE_SUCCESS, collaborator);
            }
            SpecificNote updatedNote = specificNoteRepository.findById(noteId).get();
            Note updatedCommonNote = updatedNote.getCommonNote();
            return setNoteDtoFromNote(updatedCommonNote, user);
        } catch (Exception ex) {
            log.error(NoteUtils.COLLABORATOR_DELETE_ERROR, collaboratorEmail, ex.getMessage());
            throw new CollaboratorNotDeletedException(ex);
        }
    }

    @Transactional
    public NoteDto addLabelToNote(Long userId, Long noteId, Long labelId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        try {
            Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundException());
            specificNote.getLabelSet().add(label);
            NoteDto noteDto = saveNoteAndReturnDto(user, specificNote);
            log.info(NoteUtils.NOTE_UPDATE_SUCCESS);
            kafkaProducerService.publishLabelAssignEvent(labelId, noteId, user.getEmail());
            return noteDto;
        } catch (Exception ex) {
            log.error(NoteUtils.LABEL_ADD_ERROR, labelId, ex.getMessage());
            throw new NoteNotUpdatedException(ex);
        }
    }

    @Transactional
    public NoteDto deleteLabelFromNote(Long userId, Long noteId, Long labelId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        try {
            Set<Label> labelSet = specificNote.getLabelSet();
            Set<Label> filteredLabelSet = labelSet.stream().filter(label -> {
                return !label.getId().equals(labelId);
            }).collect(Collectors.toSet());
            specificNote.setLabelSet(filteredLabelSet);
            NoteDto noteDto = saveNoteAndReturnDto(user, specificNote);
            log.info(NoteUtils.LABEL_DELETE_SUCCESS, labelId);
            kafkaProducerService.publishLabelUnassignEvent(labelId, noteId, user.getEmail());
            return noteDto;
        } catch (Exception ex) {
            log.error(NoteUtils.LABEL_DELETE_ERROR, labelId, ex.getMessage());
            throw new LabelNotDeletedException(ex);
        }
    }

    public List<NoteDto> getAllNotesForUser(Long userId) {
        User user = checkUserAndReturn(userId);
        try {
            List<SpecificNote> noteList = specificNoteRepository.findAllByUserOrderByCommonNoteCreatedAtDesc(user);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, user);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, userId, exp.getMessage());
            throw new NotesNotFoundException(exp.getMessage());

        }
    }

    public List<NoteDto> getAllNotesWithLabelsByUser(Long userId) {
        List<NoteDto> noteDtoList = getAllNotesForUser(userId);
        List<NoteDto> notesWithLabels = noteDtoList.stream().filter(noteDto -> {
            return !noteDto.getLabelSet().isEmpty();
        }).collect(Collectors.toList());
        return notesWithLabels;
    }


    public List<NoteDto> getAllNotesNonTrashedNonArchivedByUser(Long userId) {
        User user = checkUserAndReturn(userId);
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserAndIsTrashedFalseAndIsArchivedFalseOrderByCommonNoteCreatedAtDesc(user);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, user);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, user, exp.getMessage());
            throw new NotesNotFoundException(exp);

        }
    }

    public List<NoteDto> getAllNotesByIsTrashed(Long userId) {
        User user = checkUserAndReturn(userId);
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserAndIsTrashedTrueOrderByUpdatedAtDesc(user);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, user);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, user, exp.getMessage());
            throw new NoteNotFoundException(exp);
        }
    }

    public List<NoteDto> getAllNotesByIsArchived(Long userId) {
        User user = checkUserAndReturn(userId);
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserAndIsArchivedTrueOrderByCommonNoteCreatedAtDesc(user);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, user);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, user, exp.getMessage());
            throw new NoteNotFoundException(exp);
        }
    }

    public List<NoteDto> getAllNotesByReminder(Long userId) {
        User user = checkUserAndReturn(userId);
        try {
            List<SpecificNote> noteList = specificNoteRepository
                    .findAllByUserAndReminderNotNullOrderByCommonNoteCreatedAtDesc(user);
            List<NoteDto> noteDtoList = getNoteDtoFromNoteList(noteList, user);
            return noteDtoList;
        } catch (Exception exp) {
            log.error(NoteUtils.ERROR_FETCHING_NOTES_FOR_USER, user, exp.getMessage());
            throw new NoteNotFoundException(exp);
        }
    }

    public NoteDto saveNoteAndReturnDto(User user, SpecificNote note) {
        try {
            SpecificNote specificNote = specificNoteRepository.save(note);
            Note commonNote = specificNote.getCommonNote();
            log.info(NoteUtils.NOTE_UPDATE_SUCCESS);
            return setNoteDtoFromNote(commonNote, user);
        } catch (Exception ex) {
            log.error(NoteUtils.NOTE_UPDATE_ERROR, ex.getMessage());
            throw new NoteNotUpdatedException(ex);
        }
    }

    public NoteDto updateColorOfNote(Long userId, ColorUpdateDto colorDto) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, colorDto.getNoteId());
        specificNote.setColor(colorDto.getColor());
        return saveNoteAndReturnDto(user, specificNote);

    }

    public NoteDto pinNote(Long userId, Long noteId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        specificNote.setArchived(false);
        specificNote.setTrashed(false);
        specificNote.setPinned(!specificNote.isPinned());
        return saveNoteAndReturnDto(user, specificNote);

    }

    public NoteDto archiveNote(Long userId, Long noteId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        specificNote.setArchived(!specificNote.isArchived());
        specificNote.setTrashed(false);
        specificNote.setPinned(false);
        return saveNoteAndReturnDto(user, specificNote);

    }

    public NoteDto trashNote(Long userId, Long noteId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        specificNote.setArchived(false);
        specificNote.setTrashed(!specificNote.isTrashed());
        specificNote.setPinned(false);
        return saveNoteAndReturnDto(user, specificNote);
    }

    private User checkUserAndReturn(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error(UserUtils.ERROR_USER_NOT_FOUND);
            return new UserNotFoundException();
        });
        return user;
    }

    private SpecificNote checkSpecificNoteAndReturn(User user, Long noteId) {
        Optional<SpecificNote> note = specificNoteRepository.findByIdAndUser(noteId, user);
        if (!note.isPresent()) {
            log.error(NoteUtils.NOTE_NOT_FOUND, noteId, new NoteNotFoundException());
            throw new NoteNotFoundException();
        }
        return note.get();
    }

    @Transactional
    public boolean deleteNoteByUserAndId(Long userId, Long noteId) {
        User user = checkUserAndReturn(userId);
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        try {
            Note commonNote = specificNote.getCommonNote();
            List<SpecificNote> specificNoteList = commonNote.getSpecificNoteList();
            if (specificNoteList.size() == 1) {
                noteRepository.deleteNoteImages(commonNote.getId());
                noteRepository.deleteListItems(commonNote.getId());
                specificNoteRepository.deleteByIdAndUser(noteId, user);
                commonNote.getSpecificNoteList().remove(specificNote);
                commonNote.getCollaboratorList().remove(user);
                noteRepository.deleteById(commonNote.getId());
                log.info(NoteUtils.NOTE_PERMANENT_DELETE_SUCCESS);
            } else {
                commonNote.getSpecificNoteList().remove(specificNote);
                commonNote.getCollaboratorList().remove(user);
                specificNoteRepository.deleteByIdAndUser(noteId, user);
                log.info(NoteUtils.NOTE_DELETE_SUCCESS);
            }
            // Event publishing for deleting labels can be possible , OR
            // Feign call to label service for deleting all Labels for note
//            feignService.deleteAllLabelsForNote(noteId);
            log.info(NoteUtils.LABEL_DELETE_SUCCESS);
            return true;
        } catch (Exception ex) {
            log.error(NoteUtils.ERROR_DELETING_NOTE_FOR_USER, noteId, ex.getMessage());
            throw new NoteNotDeletedException(ex);
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

    private List<NoteDto> getNoteDtoFromNoteList(List<SpecificNote> noteList, User user) {
        List<NoteDto> noteDtoList = noteList.stream().map(specificNote -> {
            Note note = specificNote.getCommonNote();
            return setNoteDtoFromNote(note, user);
        }).collect(Collectors.toList());
        log.info(NoteUtils.NOTE_FETCH_SUCCESS, user);
        return noteDtoList;
    }

    private CollaboratorDto getCollaboratorDto(User user) {
        CollaboratorDto collaboratorDto = new CollaboratorDto();
        collaboratorDto.setId(user.getId());
        collaboratorDto.setName(user.getFullName());
        collaboratorDto.setEmail(user.getEmail());
        return collaboratorDto;
    }

    private NoteDto setNoteDtoFromNote(Note note, User user) {
        NoteDto noteDto = new NoteDto();
        noteDto.setTitle(note.getTitle());
        noteDto.setContent(note.getContent());
        setNoteDtoListItemsFromNoteListItems(noteDto, note.getListItems());
        noteDto.setImages(note.getImages());
        noteDto.setCreatedBy(note.getCreatedBy());
        if (note.getCollaboratorList() != null) {
            List<User> collaboratorList = note.getCollaboratorList();
            List<CollaboratorDto> collaboratorDtoList = collaboratorList.stream().map(collaboratorItem -> {
                CollaboratorDto collaboratorDto = getCollaboratorDto(collaboratorItem);
                return collaboratorDto;
            }).collect(Collectors.toList());
            noteDto.setCollaboratorList(collaboratorDtoList);
        }
        List<SpecificNote> specificNoteList = note.getSpecificNoteList();
        SpecificNote specificNote = specificNoteList.stream().filter(noteItem -> user.equals(noteItem.getUser()))
                .findFirst().get();
        noteDto.setId(specificNote.getId());
        noteDto.setColor(specificNote.getColor());
        noteDto.setCreatedAt(specificNote.getCreatedAt());
        if (note.getUpdatedAt() != null && note.getCreatedAt().compareTo(note.getUpdatedAt()) != 0) {
            noteDto.setUpdatedAt(note.getUpdatedAt());
            noteDto.setUpdatedBy(note.getUpdatedBy());
        }
        Set<Label> labelSet = specificNote.getLabelSet();
        Set<Long> labelIdsSet = labelSet.stream().map(labelItem -> labelItem.getId()).collect(Collectors.toSet());
        noteDto.setLabelSet(labelIdsSet);
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
    private Note setNoteFromNoteDto(NoteDto noteDto, Note note, User user) {
        if (note.getId() == null) {
            note.setCreatedBy(user.getId());
        }
        note.setUpdatedBy(user.getId());
        note.setTitle(noteDto.getTitle());
        note.setContent(noteDto.getContent());
        setNoteListItemsFromNoteDtoListItems(noteDto.getListItems(), note);
        note.setImages(noteDto.getImages());
        Note mappedNote = noteRepository.save(note);
        if (noteDto.getCollaboratorList() != null) {
            List<CollaboratorDto> collabDtoList = noteDto.getCollaboratorList().stream()
                    .filter(collaborator -> !collaborator.getEmail().equals(user.getEmail())).toList();
            List<String> collaboratorEmails = collabDtoList.stream().map(collaborator -> collaborator.getEmail())
                    .collect(Collectors.toList());
            Iterable<String> iterableEmails = collaboratorEmails;
            List<User> collaboratorList = userRepository.findAllByEmailIn(iterableEmails);
            List<User> nonExistingCollaboratorList = collaboratorList.stream().filter(
                            collaborator -> specificNoteRepository.findByCommonNoteAndUser(mappedNote, collaborator) == null)
                    .collect(Collectors.toList());
            nonExistingCollaboratorList.stream().forEach(collaborator -> {
                SpecificNote specificNote = new SpecificNote();
                specificNote.setCommonNote(mappedNote);
                specificNote.setUser(collaborator);
                specificNote.setRole(Utils.Role.COLLABORATOR);
                specificNoteRepository.save(specificNote);
            });
            List<User> collabList = mappedNote.getCollaboratorList();
            collabList.addAll(nonExistingCollaboratorList);
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
    private Note setSpecificNoteFromNoteDto(NoteDto noteDto, SpecificNote specificNote, User user) {
        Note note = specificNote.getCommonNote();
        if (note == null) {
            note = new Note();
        }
        Note updatedNote = setNoteFromNoteDto(noteDto, note, user);
        specificNote.setColor(noteDto.getColor());
        specificNote.setArchived(noteDto.isArchived());
        specificNote.setUpdatedAt(noteDto.getUpdatedAt());
        specificNote.setCreatedAt(noteDto.getCreatedAt());
        Set<Long> eventLabelIds = new HashSet<>();
        if (noteDto.getLabelSet() != null) {
            Set<Long> labelIds = noteDto.getLabelSet();
            Iterable<Long> iterableIds = labelIds;
            Set<Label> labelSet = labelRepository.findAllById(iterableIds).stream().collect(Collectors.toSet());
            eventLabelIds.addAll(labelSet.stream().map(label -> label.getId()).collect(Collectors.toSet()));
            specificNote.setLabelSet(labelSet);
        }
        if (specificNote.getRole() == null) {
            specificNote.setRole(Utils.Role.OWNER);
        }
        specificNote.setPinned(noteDto.isPinned());
        specificNote.setReminder(noteDto.getReminder());
        specificNote.setTrashed(noteDto.isTrashed());
        specificNote.setUser(user);
        specificNote.setCommonNote(updatedNote);
        SpecificNote savedSpecificNote = specificNoteRepository.save(specificNote);
        // Event publish on labelService side when adding , updating , deleting User Labels which LabelService will consume
        if (eventLabelIds != null)
            kafkaProducerService.publishLabelIdsAssignEvent(eventLabelIds, specificNote.getId(), user.getEmail());
        List<SpecificNote> specificNoteList = updatedNote.getSpecificNoteList();
        specificNoteList.add(savedSpecificNote);
        return updatedNote;
    }

    public NoteDto getNoteById(User user, Long noteId) {
        SpecificNote specificNote = checkSpecificNoteAndReturn(user, noteId);
        Note note = specificNote.getCommonNote();
        NoteDto noteDto = setNoteDtoFromNote(note, user);
        return noteDto;
    }

}
