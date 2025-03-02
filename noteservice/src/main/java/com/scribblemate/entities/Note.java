package com.scribblemate.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "note", indexes = { @Index(name = "index_note_created_updated", columnList = "createdAt, updatedAt") })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Note extends CommonFields {

	@Column(name = "title", length = 1000)
	private String title;

	@Column(name = "content")
	@Lob
	private String content;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> images;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "note_user_ids", joinColumns = @JoinColumn(name = "note_id"))
	@Column(name = "user_id")
	private Set<Long> userIds = new HashSet<>();

	@OneToMany(mappedBy = "commonNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<SpecificNote> specificNoteList;

	@OneToMany(mappedBy = "commonNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<ListItems> listItems;

	private Long updatedBy;

	private Long createdBy;

}
