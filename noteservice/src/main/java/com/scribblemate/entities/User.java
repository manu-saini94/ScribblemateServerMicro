package com.scribblemate.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Data
@AllArgsConstructor
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    private String profilePicture;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private Set<Label> labelSet;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<SpecificNote> specificNoteList;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinTable(name = "note_collaborator", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {
            @JoinColumn(name = "note_id")})
    private List<Note> noteList;

    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<Note> updatedByNoteList;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<Note> createdByNoteList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public User(String email) {
        this.email = email;
    }
}
