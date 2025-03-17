package com.scribblemate.repositories;

import com.scribblemate.entities.Label;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    @Transactional
    @Modifying
    @Query(value = "DELETE from note_label WHERE label_id = :labelId", nativeQuery = true)
    int deleteLabelsFromLabelNote(@Param("labelId") Long labelId);
}
