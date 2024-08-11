package com.proj.app.bookstore.controllers;

import com.proj.app.bookstore.domain.dto.UserDto;
import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.List;
import java.util.Random;
import java.util.Set;


@RestController
@RequestMapping(path = "/bookstore/users")
@RequiredArgsConstructor
public class UserController {
    private final EntityService<GroupEntity, Long> groupService;
    private final EntityService<UserEntity, Long> userService;
    private final Mapper<UserEntity, UserDto> mapper;

    @GetMapping(path = "/get")
    public ResponseEntity<Page<UserDto>> getAll(Pageable pageable){
        return ResponseEntity.ok(userService.findAll(pageable).map(mapper::mapTo));
    }

    @GetMapping(path = "/get/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable(name = "userId") Long id){
        return userService.findById(id).map(
                user -> ResponseEntity.ok(mapper.mapTo(user))
        ).orElse(
                ResponseEntity.notFound().build()
        );
    }

    @PostMapping("/update_self")
    public ResponseEntity<UserDto> update(@RequestBody UserDto userDto){
        UserEntity userToUpdate = UserEntity.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
        if(userService.findByEmail(userDto.getEmail()).isPresent())
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();

        UserEntity user = userService.partialUpdateById(getCurrentUser().getId(), userToUpdate);
        List<GroupEntity> groupsWhereUpdatedIsMember =
                groupService.findByMemberEmail(user.getEmail());
        if(!groupsWhereUpdatedIsMember.isEmpty()){
            groupsWhereUpdatedIsMember
                    .forEach(g -> {
                        g.removeMember(getCurrentUser());
                        g.addMember(user);
                        if(g.getElderUsers().contains(user)){
                            g.removeMember(getCurrentUser());
                            g.addMember(user);
                        }
                        if(g.getAdminUser().equals(user))
                            g.setAdminUser(user);
                        groupService.save(g);
                    });
        }

        return ResponseEntity.ok(mapper.mapTo(userService.save(user)));
    }

    @DeleteMapping("/delete_self")
    public ResponseEntity<UserDto> deleteSelf(){
        //get the user that wants to remove itself from the jwt token with email
        UserEntity toBeRemoved = getCurrentUser();
        UserDto removed = deleteUser(toBeRemoved);
        return ResponseEntity.ok(removed);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("delete/{userId}")
    public ResponseEntity<UserDto> deleteById(@PathVariable("userId") Long userId){
        if(userService.findById(userId).isEmpty())
            return ResponseEntity.notFound().build();
        UserEntity toBeRemoved = userService.findById(userId).get();
        UserDto removed = deleteUser(toBeRemoved);
        return ResponseEntity.ok(removed);
    }

    //this marks the start of helper methods

    /**
     * This method makes a random user in a group into admin. <br>
     * The priority is given to {@code group.elderUsers} then {@code group.Members}
     * @param group this is the group we want to update with new admin user
     */
    private void makeRandomUserAdmin(GroupEntity group){
        // check if a group has elderUsers. if true make copy them in the set else the set becomes a set of members
        Set<UserEntity> eldersOrMembers = (group.getElderUsers().isEmpty())? group.getMembers() : group.getElderUsers();

        //if the size of the user set is 1 we set the random int as 0, to not get illegalArgumentException from nextInt
        int rand = (eldersOrMembers.size() == 1)? 0:new Random().nextInt(0, eldersOrMembers.size() - 1);
        UserEntity toBeMadeAdmin = userService.findByEmail(eldersOrMembers
                .stream()
                .toList()
                .get(rand)
                .getEmail()).get();
        toBeMadeAdmin.addRole("GROUP_ADMIN_" + group.getId());
        if(group.getElderUsers().isEmpty())
            toBeMadeAdmin.addRole("GROUP_ELDER_" + group.getId());
        group.setAdminUser(userService.save(toBeMadeAdmin));
        groupService.save(group);
    }

    private UserEntity getCurrentUser(){
        return userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalArgumentException("jwt unauthenticated"));
    }

    /**
     * Deletes the user with email specified by Json Web Token <br>
     * <br>
     * This method performs safe deletion. Removing the user from all the groups they were part off,
     * assigning groups where they were admin new random admin and removing from {@code DB}
     * @return The user we want to delete.
     */
    private UserDto deleteUser(UserEntity toBeRemoved){
        List<GroupEntity> groupsWhereDeletedIsMember =
                groupService.findByMemberEmail(toBeRemoved.getEmail());

        //check if the user is a member of any group
        if(!groupsWhereDeletedIsMember.isEmpty()) {

            //update each group after deleting the user we want to delete
            groupsWhereDeletedIsMember
                    .forEach(a -> {
                        a.removeMember(toBeRemoved);
                        if(a.getMembers().isEmpty()){
                            groupService.deleteById(a.getId());
                            return;
                        }
                        if(a.getElderUsers().contains(toBeRemoved)){
                            a.removeElder(toBeRemoved);
                        }
                        if(a.getAdminUser().equals(toBeRemoved)){
                            makeRandomUserAdmin(a);
                        }
                        groupService.save(a);
                    });
        }

        UserEntity removed = userService.deleteById(toBeRemoved.getId());
        return mapper.mapTo(removed);
    }
}
