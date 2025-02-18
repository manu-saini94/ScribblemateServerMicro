package com.scribblemate.repositories;

import com.scribblemate.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findById(Long userId);

//	List<User> findAllByEmail(Iterable<String> emails);

	List<User> findAllByEmailIn(Iterable<String> emails);

	@Query("SELECT u.id, u.createdAt, u.email, u.fullName, u.profilePicture, u.status, u.updatedAt FROM User u WHERE u.email = :email")
	List<Object[]> findSpecificFieldsByEmail(@Param("email") String email);
}
