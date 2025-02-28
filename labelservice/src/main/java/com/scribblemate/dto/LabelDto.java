package com.scribblemate.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class LabelDto {

    private Long id;

    private String labelName;

    private boolean isImportant;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<Long> noteIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean isImportant) {
        this.isImportant = isImportant;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Long> getNoteIds() {
        return noteIds;
    }

    public void setNoteIds(Set<Long> noteIds) {
        this.noteIds = noteIds;
    }

    public LabelDto(Long id, String labelName, boolean isImportant, LocalDateTime createdAt, LocalDateTime updatedAt, Set<Long> noteIds) {
        this.id = id;
        this.labelName = labelName;
        this.isImportant = isImportant;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.noteIds = noteIds;
    }

    public LabelDto() {
        super();

    }

}
