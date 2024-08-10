package com.proj.app.bookstore.controllers;

import com.proj.app.bookstore.domain.dto.BookDto;
import com.proj.app.bookstore.domain.dto.GroupDto;
import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.GroupService;
import com.proj.app.bookstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = "/bookstore/group/")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final Mapper<GroupEntity, GroupDto> mapper;
    private final UserService userService;
    private final Mapper<BookEntity, BookDto> bookMapper;


    @PostMapping("/register")
    public ResponseEntity<GroupDto> registerGroup(@RequestBody GroupDto groupDto){
        GroupEntity group = mapper.mapFrom(groupDto);
        UserEntity currentUser = getCurrentUser();
        group = groupService.save(group);
        group = addNewUser(group, currentUser, GroupRoles.ADMIN);
        GroupDto savedDto = mapper.mapTo(group);
        return ResponseEntity.ok(savedDto);
    }

    @PreAuthorize("hasAnyAuthority('GROUP_ADMIN_' + #groupId, 'GROUP_ELDER_' + #groupId)")
    @GetMapping("/get/{groupId}")
    public ResponseEntity<GroupDto> getGroup(@PathVariable("groupId") Long groupId){
        Optional<GroupEntity> currentGroupOpt = groupService.getById(groupId);
        if(currentGroupOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        GroupEntity currentGroup = currentGroupOpt.get();
        return ResponseEntity.ok(mapper.mapTo(currentGroup));
    }

    @GetMapping("/get")
    public Page<GroupDto> getGroups(Pageable pageable){
        return groupService.getAll(pageable).map(mapper::mapTo);
    }

    @PreAuthorize("hasAnyAuthority('GROUP_ADMIN_' + #groupId, 'GROUP_ELDER_' + #groupId)")
    @PatchMapping("/add_member/{groupId}")
    public ResponseEntity<GroupDto> addMember(@PathVariable("groupId") Long groupId,
                                             @RequestParam(name = "userId") Long userId){
        Pair<GroupEntity, UserEntity> pair;
        try {
            pair = checkValidityAndReturn(groupId, userId);
        } catch (IllegalArgumentException i){
            return ResponseEntity.notFound().build();
        }
        GroupEntity currentGroup =  pair.getFirst();
        UserEntity currentUser = pair.getSecond();

        if(currentGroup.getMembers().contains(currentUser))
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

        currentGroup = addNewUser(currentGroup, currentUser, GroupRoles.USER);

        var result = mapper.mapTo(currentGroup);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('GROUP_ADMIN_' + #groupId)")
    @PatchMapping("/add_elder/{groupId}")
    public ResponseEntity<GroupDto> addElder(@PathVariable("groupId") Long groupId,
                                              @RequestParam(name = "userId") Long userId){
        Pair<GroupEntity, UserEntity> pair;
        try {
            pair = checkValidityAndReturn(groupId, userId);
        } catch (IllegalArgumentException i){
            return ResponseEntity.notFound().build();
        }
        GroupEntity currentGroup =  pair.getFirst();
        UserEntity currentUser = pair.getSecond();

        if(currentGroup.getElderUsers().contains(currentUser))
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

        currentGroup = addNewUser(currentGroup, currentUser, GroupRoles.ELDER);

        var result = mapper.mapTo(currentGroup);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('GROUP_ADMIN_' + #groupId)")
    @PatchMapping("/change_admin/{groupId}")
    public ResponseEntity<GroupDto> changeAdmin(@PathVariable("groupId") Long groupId,
                                                @RequestParam("userId") Long userId){
        Pair<GroupEntity, UserEntity> pair;
        try {
            pair = checkValidityAndReturn(groupId, userId);
        } catch (IllegalArgumentException i){
            return ResponseEntity.notFound().build();
        }
        GroupEntity currentGroup =  pair.getFirst();
        UserEntity currentUser = pair.getSecond();

        UserEntity currentAdmin = getCurrentUser();

        if(currentGroup.getAdminUser().equals(currentUser)
                || !currentGroup.getMembers().contains(currentUser))
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();

        currentAdmin.removeRole("GROUP_ADMIN_" + currentGroup.getId());
        userService.save(currentAdmin);

        currentGroup = addNewUser(currentGroup, currentUser, GroupRoles.ELDER);

        var result = mapper.mapTo(currentGroup);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('GROUP_ADMIN_' + #groupId)")
    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId){
        //check group validity
        Optional<GroupEntity> groupOpt = groupService.getById(groupId);
        if(groupOpt.isEmpty())
            return ResponseEntity.notFound().build();
        var group = groupOpt.get();
        //remove "GROUP_MEMBER_" and "GROUP_ELDER_" roles from members
        Set<UserEntity> members = group.getMembers();
        members.stream().map(a -> userService.findByEmail(a.getEmail()).get())
                .forEach(a -> {
                    a.removeRole("GROUP_MEMBER_" + group.getId());
                    if(a.getRoles().contains("GROUP_ELDER_" + group.getId()))
                        a.removeRole("GROUP_ELDER_"+group.getId());
                    userService.save(a);
                });
        //remove "GROUP_ADMIN_" role from admin
        var admin = userService.findById(getCurrentUser().getId()).get();
        admin.removeRole("GROUP_ADMIN_" + group.getId());
        userService.save(admin);
        //Delete the group.
        groupService.deleteById(groupId);
        //returns deleted group name.
        return ResponseEntity.ok(group.getName());
    }

    @PreAuthorize("hasAuthority('GROUP_MEMBER_') + #groupId")
    @GetMapping("/members_books")
    public Page<BookDto> getMemberBooks(@RequestParam("groupId") Long groupId, Pageable pageable){
        return userService
                .getAllBooks(groupService.getById(groupId).get().getMembers(), pageable)
                .map(bookMapper::mapTo);
    }

    // this marks the start for helper methods

    /**
     * @return The current user from the email specified in the {@code JWT token}.
     */
    private UserEntity getCurrentUser(){
        return userService
                .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get();
    }

    /**
    * Adds roles specified by {@code roles} to the {@code user} and saves both the group and the user in DB.
     * @param group The group we want to change.
     * @param user The user we want to add roles for
     * @param roles The roles we want to add to the user. {@link GroupRoles}
     * @return The updated GroupEntity
    */
    private GroupEntity addNewUser(GroupEntity group, UserEntity user, GroupRoles roles){
        switch(roles){
            case GroupRoles.ADMIN -> {
                user.addRole("GROUP_ADMIN_" + group.getId());
                user.addRole("GROUP_ELDER_" + group.getId());
                user.addRole("GROUP_MEMBER_" + group.getId());

                group.setAdminUser(userService.save(user));
                group.addElderUser(userService.save(user));
                group.addMember(userService.save(user));
            }
            case GroupRoles.ELDER ->{
                user.addRole("GROUP_ELDER_" + group.getId());
                user.addRole("GROUP_MEMBER_" + group.getId());

                group.addElderUser(userService.save(user));
                group.addMember(userService.save(user));
            }
            default ->{
                user.addRole("GROUP_MEMBER_" + group.getId());

                group.addMember(userService.save(user));
            }
        }
        return groupService.save(group);
    }

    /**
     * Checks if the group and the user with these params exists in DB.
     * @param groupId GroupID we'd like to validate.
     * @param userId UserID we'd like to validate.
     * @return A Pair of {@link GroupEntity} and {@link UserEntity} validity test passes.
     * @throws IllegalArgumentException if one of the two aren't valid.
     */
    private Pair<GroupEntity, UserEntity> checkValidityAndReturn
            (Long groupId, Long userId){
        Optional<GroupEntity> groupOpt = groupService.getById(groupId);
        Optional<UserEntity> userOpt = userService.findById(userId);

        if(groupOpt.isEmpty() || userOpt.isEmpty())
            throw new IllegalArgumentException("one of the fields are empty");

        return Pair.of(groupOpt.get(), userOpt.get());
    }

    /**
     * Enum I use to change roles for a user.     */
    private enum GroupRoles{
        ELDER,ADMIN,USER
    }
}
