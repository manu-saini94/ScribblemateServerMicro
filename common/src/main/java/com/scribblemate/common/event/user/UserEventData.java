package com.scribblemate.common.event.user;

import com.scribblemate.common.utility.Utils;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEventData {
    private Long id;

    private String fullName;

    private String email;

    private Utils.Status status;

    private String profilePicture;
}
