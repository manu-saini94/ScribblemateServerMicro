package com.scribblemate.repositories;

import com.scribblemate.entities.SpecificNote;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificNoteRepository extends CrudRepository<SpecificNote, Long> {

    @Transactional
    @Modifying
    @Query(value = "DELETE from note_label WHERE label_id = :labelId", nativeQuery = true)
    int deleteLabelsFromLabelNote(@Param("labelId") Long labelId);
}
