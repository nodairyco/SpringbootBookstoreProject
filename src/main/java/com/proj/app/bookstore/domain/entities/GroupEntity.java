package com.proj.app.bookstore.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "groups")
public class GroupEntity {
    @Id
    @SequenceGenerator(
            name = "group_id_sequence",
            sequenceName = "group_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            generator = "group_id_sequence",
            strategy = GenerationType.SEQUENCE
    )
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity adminUser;

    @ManyToMany
    @JoinTable(
            name = "elder_user",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "email")
    )
    private Set<UserEntity> elderUsers;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "email")
    )
    private Set<UserEntity> members;

    public void addMember(UserEntity user){
        members.add(user);
    }

    public void addElderUser(UserEntity user){
        elderUsers.add(user);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, id);
    }

    @Override
    public String toString(){
        return id + name + adminUser.toString();
    }

    public void removeElder(UserEntity currentUser) {
        elderUsers.remove(currentUser);
    }

    public void removeMember(UserEntity user){
        members.remove(user);
    }
}
