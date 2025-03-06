package com.scribblemate.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scribblemate.entities.Note;
import com.scribblemate.entities.SpecificNote;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface SpecificNoteRepository extends JpaRepository<SpecificNote, Long> {

    Optional<SpecificNote> findById(Long id);

    Optional<SpecificNote> findByIdAndUserId(Long noteId, Long userId);

    List<SpecificNote> findAllByUserIdAndIsTrashedFalseAndIsArchivedFalseOrderByCommonNoteCreatedAtDesc(Long userId);

    List<SpecificNote> findAllByUserIdOrderByCommonNoteCreatedAtDesc(Long userId);

    SpecificNote findByCommonNoteAndUserId(Note note, Long userId);

    List<SpecificNote> findAllByUserIdAndIsTrashedTrueOrderByUpdatedAtDesc(Long userId);

    List<SpecificNote> findAllByUserIdAndIsArchivedTrueOrderByCommonNoteCreatedAtDesc(Long userId);

    List<SpecificNote> findAllByUserIdAndReminderNotNullOrderByCommonNoteCreatedAtDesc(Long userId);

    List<SpecificNote> findAllByCommonNote(Note commonNote);

    @Transactional
    @Modifying
    @Query(value = "DELETE from specific_note WHERE common_note_id = :commonNoteId and user_id = :userId", nativeQuery = true)
    void deleteByCommonNoteIdAndUserId(@Param("commonNoteId") Long commonNoteId, @Param("userId") Long userId);

    @Transactional
    void deleteByCommonNoteAndUserId(Note commonNote, Long userId);

    @Transactional
    void deleteAllByUserId(Long userId);

    @Transactional
    @Modifying
    int deleteByIdAndUserId(Long noteId, Long userId);


}
