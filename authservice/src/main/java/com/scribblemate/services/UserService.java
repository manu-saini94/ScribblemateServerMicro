package com.scribblemate.services;

import java.util.List;
import java.util.stream.Collectors;
import com.scribblemate.exceptions.UserNotDeletedException;
import com.scribblemate.exceptions.UserNotFoundException;
import com.scribblemate.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scribblemate.dto.CollaboratorDto;
import com.scribblemate.dto.UserResponseDto;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtAuthenticationService jwtService;

    public List<UserResponseDto> getAllUsers() {
        List<User> users = (List<User>) userRepository.findAll();
        List<UserResponseDto> usersDtoList = users.stream().map(user -> getUserDtoFromUser(user))
                .collect(Collectors.toList());
        return usersDtoList;
    }


    @Transactional
    public boolean deleteUser(User currentUser) {
        try {
            User user = userRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new UserNotFoundException());
//			if (!user.getLabelSet().isEmpty()) {
//				user.getLabelSet().forEach(label -> specificNoteRepository.deleteLabelsFromLabelNote(label.getId()));
//			}
//			labelRepository.deleteAllByUser(user);
//			user.getNoteList().forEach(note -> {
//				if (!note.getCollaboratorList().isEmpty()) {
//					List<User> userList = note.getCollaboratorList().stream().filter(item -> !item.equals(user))
//							.toList();
//					note.setCollaboratorList(userList);
//				}
//				if (!note.getSpecificNoteList().isEmpty()) {
//					List<SpecificNote> noteList = note.getSpecificNoteList().stream()
//							.filter(item -> !item.getUser().equals(user)).toList();
//					note.setSpecificNoteList(noteList);
//				}
//				noteRepository.save(note);
//			});
//			user.getLabelSet().clear();
            user.setStatus(Utils.Status.INACTIVE);
            userRepository.save(user);
            return true;
        } catch (Exception exp) {
            // TODO Auto-generated catch block
//			log.error(NoteUtils.ERROR_DELETING_USER, exp);
            throw new UserNotDeletedException(exp.getMessage());
        }
    }

    public UserResponseDto getUserDtoFromUser(User user) {
        UserResponseDto userDto = new UserResponseDto();
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
