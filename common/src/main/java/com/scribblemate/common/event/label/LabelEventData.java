package com.scribblemate.common.event.label;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LabelEventData {

    private Long id;

    private String labelName;

    private boolean isImportant;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
