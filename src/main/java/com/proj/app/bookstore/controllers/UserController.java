package com.proj.app.bookstore.controllers;

import com.proj.app.bookstore.domain.dto.UserDto;
import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.AuthenticationService;
import com.proj.app.bookstore.services.GroupService;
import com.proj.app.bookstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


@RestController
@RequestMapping(path = "/bookstore/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final Mapper<UserEntity, UserDto> mapper;
    private final AuthenticationService auth;
    private final GroupService groupService;

    @GetMapping(path = "/get")
    public ResponseEntity<Page<UserDto>> getAll(Pageable pageable){
        return ResponseEntity.ok(userService.getAll(pageable).map(mapper::mapTo));
    }

    @GetMapping(path = "/get/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable(name = "userId") Long id){
        return userService.getUserById(id).map(
                user -> ResponseEntity.ok(mapper.mapTo(user))
        ).orElse(
                ResponseEntity.notFound().build()
        );
    }

    @DeleteMapping("/delete_self")
    public ResponseEntity<UserDto> deleteSelf(){
        //get the user that wants to remove itself from the jwt token with email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity toBeRemoved = userService.findByEmail(email).get();


        List<GroupEntity> groupsWhereDeletedIsMember =
                groupService.findByMemberEmail(toBeRemoved.getEmail());
        //check if the user is a member of any group
        if(!toBeRemoved.getRoles()
                .stream()
                .filter(a -> a.startsWith("GROUP_MEMBER_"))
                .toList()
                .isEmpty()) {

            //update each group after deleting the user we want to delete
            groupsWhereDeletedIsMember
                    .forEach(a -> {
                        a.removeMember(toBeRemoved);
                        if(a.getMembers().isEmpty()){
                            groupService.deleteById(a.getId());
                            toBeRemoved.setRoles(new HashSet<>());
                            return;
                        }
                        groupService.save(a);
                    });
        }

        //check if user is an elder in any group
        if(!toBeRemoved.getRoles()
                .stream()
                .filter(a -> a.startsWith("GROUP_ELDER_"))
                .toList()
                .isEmpty()) {
            //update each group after deleting the user we want to delete
            groupsWhereDeletedIsMember
                    .stream()
                    .filter(a -> a.getElderUsers().contains(toBeRemoved))
                    .forEach(a -> {
                        a.removeElder(toBeRemoved);
                        groupService.save(a);
                    });
        }

        //check if user is an admin in any group
        if(!toBeRemoved.getRoles()
                .stream()
                .filter(a -> a.startsWith("GROUP_ADMIN_"))
                .toList()
                .isEmpty()) {
            //get all the groups where the user is admin
            List<GroupEntity> groupsWhereDeletedIsAdmin = groupService.findByAdminEmail(email);
            //get a new random admin from elders if elders != empty, else get it from members
            //if members == empty remove group
            groupsWhereDeletedIsAdmin
                    .forEach(a -> {
                        if (a.getMembers().isEmpty()) {
                            groupService.deleteById(a.getId());
                            return;
                        }
                        if (a.getElderUsers().isEmpty()) {
                            // Get random user from group Members
                            UserEntity toBeMadeAdmin;
                            try {
                                 toBeMadeAdmin = userService.findByEmail(a.getMembers()
                                        .stream()
                                        .toList()
                                        .get(
                                                new Random().nextInt(0, a.getMembers().size() - 1)
                                        )
                                        .getEmail()).get();
                            } catch(IllegalArgumentException i){
                                 toBeMadeAdmin = userService.findByEmail(a.getMembers()
                                        .stream()
                                        .toList()
                                        .getFirst()
                                        .getEmail()).get();
                            }
                            toBeMadeAdmin.addRole("GROUP_ADMIN_" + a.getId());
                            toBeMadeAdmin.addRole("GROUP_ELDER_" + a.getId());
                            a.setAdminUser(userService.save(toBeMadeAdmin));
                            groupService.save(a);
                            return;
                        }
                        try {
                            UserEntity toBeMadeAdmin = userService.findByEmail(a.getElderUsers()
                                    .stream()
                                    .toList()
                                    .get(new Random().nextInt(0, a.getElderUsers().size() - 1))
                                    .getEmail()).get();
                            toBeMadeAdmin.addRole("GROUP_ADMIN_" + a.getId());
                            a.setAdminUser(userService.save(toBeMadeAdmin));
                            groupService.save(a);
                        }catch (IllegalArgumentException i){
                            UserEntity toBeMadeAdmin = userService.findByEmail(a.getElderUsers()
                                    .stream()
                                    .toList()
                                    .getFirst()
                                    .getEmail()).get();
                            toBeMadeAdmin.addRole("GROUP_ADMIN_" + a.getId());
                            a.setAdminUser(userService.save(toBeMadeAdmin));
                            groupService.save(a);
                        }
                    });
        }
        UserEntity removed = userService.deleteById(toBeRemoved.getId());
        return ResponseEntity.ok(mapper.mapTo(removed));
    }
}
