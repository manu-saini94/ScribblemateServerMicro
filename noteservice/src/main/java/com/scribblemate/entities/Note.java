package com.scribblemate.entities;

import java.util.ArrayList;
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
	private List<String> images = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "note_user",
			joinColumns = @JoinColumn(name = "note_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private List<User> collaboratorList = new ArrayList<>();

	@OneToMany(mappedBy = "commonNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<SpecificNote> specificNoteList = new ArrayList<>();

	@OneToMany(mappedBy = "commonNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<ListItems> listItems = new ArrayList<>();

	private Long updatedBy;

	private Long createdBy;

}
