package com.scribblemate.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "label", indexes = {@Index(name = "index_user_labelName", columnList = "userId,labelName")})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Label extends CommonFields {

    @Column(name = "label_name")
    private String labelName;

    @Column(name = "is_important")
    @JsonProperty(value = "isImportant")
    private boolean isImportant;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "label_note_ids", joinColumns = @JoinColumn(name = "label_id"))
    @Column(name = "note_id")
    private Set<Long> noteIds = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Override
    public int hashCode() {
        return Objects.hash(labelName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Label other = (Label) obj;
        return Objects.equals(labelName, other.labelName);
    }

    @Override
    public String toString() {
        return "Label{" +
                "labelName='" + labelName + '\'' +
                ", isImportant=" + isImportant +
                ", user=" + user +
                '}';
    }
}
