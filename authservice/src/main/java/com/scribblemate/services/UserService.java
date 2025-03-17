package com.scribblemate.services;

import java.util.List;
import java.util.stream.Collectors;

import com.scribblemate.common.dto.CollaboratorDto;
import com.scribblemate.common.utility.ResponseErrorUtils;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import com.scribblemate.exceptions.UserNotDeletedException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.utility.Utils;
import com.scribblemate.exceptions.UserNotUpdatedException;
import com.scribblemate.exceptions.UsersFetchException;
import com.scribblemate.common.utility.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.common.dto.UserDto;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtAuthenticationService jwtService;

    @Autowired
    private KafkaProducerService kafkaService;

    public List<UserDto> getAllUsers() {
        try {
            List<User> users = (List<User>) userRepository.findAll();
            List<UserDto> usersDtoList = users.stream().map(user -> getUserDtoFromUser(user))
                    .collect(Collectors.toList());
            log.info(ResponseSuccessUtils.FETCH_ALL_USERS_SUCCESS);
            return usersDtoList;
        } catch (Exception e) {
            log.error(ResponseErrorUtils.FETCH_ALL_USERS_ERROR.getMessage());
            throw new UsersFetchException(e.getMessage());
        }
    }

    @Transactional
    public UserDto updateUserDetails(UserDto userDto, User currentUser) {
        try {
            User user = userRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new UserNotFoundException());
            user.setFullName(userDto.getFullName());
            user.setProfilePicture(userDto.getProfilePicture());
            User savedUser = userRepository.save(user);
            UserDto userDetailsDto = getUserDtoFromUser(savedUser);
            log.info(ResponseSuccessUtils.USER_PERSIST_SUCCESS);
            kafkaService.publishUserUpdatedEvent(savedUser);
            return userDetailsDto;
        } catch (Exception exp) {
            log.error(UserUtils.ERROR_PERSISTING_USER, currentUser, exp.getMessage());
            throw new UserNotUpdatedException(exp.getMessage());
        }
    }


    @Transactional
    public UserDto deactivateUser(User currentUser) {
        try {
            User user = userRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new UserNotFoundException());
            user.setStatus(Utils.Status.INACTIVE);
            User savedUser = userRepository.save(user);
            UserDto userDetailsDto = getUserDtoFromUser(savedUser);
            log.info(ResponseSuccessUtils.USER_DEACTIVATE_SUCCESS);
//            kafkaService.publishUserUpdatedEvent(savedUser);
            return userDetailsDto;
        } catch (Exception exp) {
            log.error(UserUtils.ERROR_PERSISTING_USER, currentUser, exp.getMessage());
            throw new UserNotUpdatedException(exp.getMessage());
        }
    }

    @Transactional
    public UserDto activateUser(User currentUser) {
        try {
            User user = userRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new UserNotFoundException());
            user.setStatus(Utils.Status.ACTIVE);
            User savedUser = userRepository.save(user);
            UserDto userDetailsDto = getUserDtoFromUser(savedUser);
            log.info(ResponseSuccessUtils.USER_ACTIVATE_SUCCESS);
            return userDetailsDto;
        } catch (Exception exp) {
            log.error(UserUtils.ERROR_PERSISTING_USER, currentUser, exp.getMessage());
            throw new UserNotUpdatedException(exp.getMessage());
        }
    }

    @Transactional
    public boolean deleteUser(User currentUser) {
        try {
            User user = userRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new UserNotFoundException());
            userRepository.delete(user);
            log.info(ResponseSuccessUtils.USER_DELETE_SUCCESS);
            kafkaService.publishUserDeletedEvent(user);
            return true;
        } catch (Exception exp) {
            log.error(UserUtils.ERROR_DELETING_USER, currentUser, exp.getMessage());
            throw new UserNotDeletedException(exp.getMessage());
        }
    }


    public UserDto getUserDtoFromUser(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFullName(user.getFullName());
        userDto.setProfilePicture(user.getProfilePicture());
        userDto.setStatus(user.getStatus());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        return userDto;
    }

    public CollaboratorDto getCollaboratorDtoFromUser(User user) {
        CollaboratorDto collaboratorDto = new CollaboratorDto();
        collaboratorDto.setEmail(user.getEmail());
        collaboratorDto.setName(user.getFullName());
        collaboratorDto.setId(user.getId());
        return collaboratorDto;
    }

    public User getUserFromJwt(String jwt) {
        final String userEmail = jwtService.extractUsername(jwt);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
        return user;
    }

    public CollaboratorDto checkForUserExist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with this email does not exist"));
        CollaboratorDto collaboratorDto = getCollaboratorDtoFromUser(user);
        return collaboratorDto;
    }

}
