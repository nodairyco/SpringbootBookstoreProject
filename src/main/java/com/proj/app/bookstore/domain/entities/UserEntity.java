package com.proj.app.bookstore.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {
    @Id
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_id_sequence"
    )
    private Long id;

    @JsonProperty("user_name")
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "email"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "elderUsers")
    private Set<GroupEntity> elderInGroups = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private Set<GroupEntity> membersIn = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "purchased_books",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "isbn")
    )
    private Set<BookEntity> purchasedBooks;

    private Integer money = 0;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public void addRole(String role){
        roles.add(role);
    }

    public void addElderInGroup(GroupEntity group){
        elderInGroups.add(group);
    }

    public void addMemberIn(GroupEntity group){
        membersIn.add(group);
    }

    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, name, email, password);
    }

    @Override
    public String toString(){
        return id + name + email;
    }

    public void removeRole(String role){
        roles.remove(role);
    }
}
