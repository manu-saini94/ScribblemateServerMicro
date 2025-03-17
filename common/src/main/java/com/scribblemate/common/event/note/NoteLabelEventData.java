package com.scribblemate.common.event.note;
import lombok.*;

import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NoteLabelEventData {
    private Long id;
    private Long labelId;
}
