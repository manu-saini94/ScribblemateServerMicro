package com.scribblemate.common.event.note;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NoteLabelIdsEventData {
    private Long id;
    private Set<Long> labelIds;
}
