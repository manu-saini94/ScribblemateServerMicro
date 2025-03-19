package com.scribblemate.repositories;

import java.util.Optional;
import java.util.Set;

import com.scribblemate.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.scribblemate.entities.Label;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByIdAndUserId(Long id, Long userId);

    @Query(value = "SELECT * from label WHERE user_id = :userId and label_name = :labelName", nativeQuery = true)
    Label findByUserIdAndLabelName(@Param("userId") Long userId, @Param("labelName") String labelName);

    @Transactional
    @Modifying
    int deleteByIdAndUserId(Long labelId, Long userId);

//	@Transactional
//	void deleteAllByUser(User user);

    @Query(value = "SELECT id from label WHERE user_id = :userId", nativeQuery = true)
    Set<Long> getLabelIdsByUser(@Param("userId") Long userId);

    @Query(value = "SELECT * from label WHERE user_id = :userId", nativeQuery = true)
    Set<Label> findAllByUserIdOrderByLabelName(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO label_note_ids (label_id, note_id) " +
            "SELECT lb.id, :noteId FROM label lb WHERE lb.id IN :labelIds", nativeQuery = true)
    int addLabelIdsToNote(@Param("labelIds") Set<Long> labelIds, @Param("noteId") Long noteId);

    @Query("SELECT lb.id FROM Label lb WHERE :noteId MEMBER OF lb.noteIds")
    Set<Long> findLabelIdsByNoteId(@Param("noteId") Long noteId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO label_note_ids (label_id, note_id) VALUES (:labelId, :noteId)", nativeQuery = true)
    int addLabelToNote(@Param("labelId") Long labelId, @Param("noteId") Long noteId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM label_note_ids lbn WHERE lbn.label_id = :labelId AND lbn.note_id = :noteId ", nativeQuery = true)
    int deleteLabelFromNote(@Param("labelId") Long labelId, @Param("noteId") Long noteId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM label_note_ids lbn WHERE  lbn.note_id = :noteId ", nativeQuery = true)
    int deleteNoteReferences(@Param("noteId") Long noteId);

    @Modifying
    @Transactional
    @Query(value = "DELETE ln FROM label_note_ids ln JOIN label lb ON ln.label_id = lb.id WHERE lb.user_id = :userId AND ln.note_id = :noteId", nativeQuery = true)
    int deleteLabelIdsByNoteId(@Param("noteId") Long noteId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    int deleteAllByUser(User user);


}
