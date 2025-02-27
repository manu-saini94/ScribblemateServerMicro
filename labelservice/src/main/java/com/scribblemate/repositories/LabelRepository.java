package com.scribblemate.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.scribblemate.entities.Label;
import jakarta.transaction.Transactional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

//	Label findByIdAndUser(Long id, User user);

    @Query(value = "SELECT * from label WHERE user_id = :userId and label_name = :labelName", nativeQuery = true)
    Label findByUserIdAndLabelName(@Param("userId") Long userId, @Param("labelName") String labelName);

    @Transactional
    @Modifying
    @Query(value = "DELETE from label WHERE id = :labelId and user_id = :userId", nativeQuery = true)
    int deleteByIdAndUser(@Param("labelId") Long labelId, @Param("userId") Long userId);

//	@Transactional
//	void deleteAllByUser(User user);

    @Query(value = "SELECT id from label WHERE user_id = :userId", nativeQuery = true)
    List<Long> getLabelIdsByUser(@Param("userId") Long userId);

    @Query(value = "SELECT * from label WHERE user_id = :userId", nativeQuery = true)
    List<Label> findAllByUserIdOrderByLabelName(@Param("userId") Long userId);

//	Label findByUserAndLabelName(User user, String labelName);
}
