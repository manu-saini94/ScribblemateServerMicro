package com.scribblemate.user.event;

import com.scribblemate.common.utility.Utils;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreatedEvent {
    private Long id;

    private String fullName;

    private String email;

    private Utils.Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String profilePicture;
}
