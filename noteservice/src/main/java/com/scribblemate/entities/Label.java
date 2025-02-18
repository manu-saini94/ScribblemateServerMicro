package com.scribblemate.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.List;

@Entity
@Table(name = "label", indexes = {@Index(name = "index_user_labelName", columnList = "userId,labelName")})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @ManyToMany(mappedBy = "labelSet", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE})
    private List<SpecificNote> noteList;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

}
