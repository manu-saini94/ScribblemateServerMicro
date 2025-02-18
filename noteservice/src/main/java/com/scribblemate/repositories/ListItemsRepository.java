package com.scribblemate.repositories;

import com.scribblemate.entities.ListItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListItemsRepository extends JpaRepository<ListItems, Integer> {

}
