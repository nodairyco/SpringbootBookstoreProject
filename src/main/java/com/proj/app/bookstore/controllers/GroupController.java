package com.proj.app.bookstore.controllers;

import com.proj.app.bookstore.domain.dto.BookDto;
import com.proj.app.bookstore.domain.dto.GroupDto;
import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.repositories.GroupRepository;
import com.proj.app.bookstore.repositories.UserRepository;
import com.proj.app.bookstore.services.GroupService;
import com.proj.app.bookstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        String contextPrincipalEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> currentUserOpt = userService
                .findByEmail(contextPrincipalEmail);
        if(currentUserOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var currentUser = currentUserOpt.get();
        GroupEntity group = mapper.mapFrom(groupDto);
        group.setAdminUser(currentUser);

        GroupEntity saved = groupService.save(group);
        currentUser.addRole("GROUP_ADMIN_" + saved.getId());
        currentUser.addRole("GROUP_ELDER_" + saved.getId());
        currentUser.addRole("GROUP_MEMBER_" + saved.getId());
        saved.setAdminUser(userService.save(currentUser));
        saved.addElderUser(currentUser);
        saved.addMember(currentUser);
        GroupDto savedDto = mapper.mapTo(groupService.save(group));
        currentUser.addElderInGroup(mapper.mapFrom(savedDto));
        currentUser.addMemberIn(mapper.mapFrom(savedDto));
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
        Optional<GroupEntity> groupOpt = groupService.getById(groupId);
        Optional<UserEntity> userOpt = userService.getUserById(userId);

        if(groupOpt.isEmpty() || userOpt.isEmpty())
            return ResponseEntity.notFound().build();

        GroupEntity currentGroup = groupOpt.get();
        UserEntity currentUser = userOpt.get();

        if(currentGroup.getMembers().contains(currentUser))
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

        currentUser.addRole("GROUP_MEMBER_" + currentGroup.getId());
        currentGroup.addMember(userService.save(currentUser));
        currentUser.addMemberIn(currentGroup);
        userService.save(currentUser);

        return ResponseEntity.ok(mapper.mapTo(groupService.save(currentGroup)));
    }

    @PreAuthorize("hasAnyAuthority('GROUP_ADMIN_' + #groupId)")
    @PatchMapping("/add_elder/{groupId}")
    public ResponseEntity<GroupDto> addElder(@PathVariable("groupId") Long groupId,
                                              @RequestParam(name = "userId") Long userId){
        Optional<GroupEntity> groupOpt = groupService.getById(groupId);
        Optional<UserEntity> userOpt = userService.getUserById(userId);

        if(groupOpt.isEmpty() || userOpt.isEmpty())
            return ResponseEntity.notFound().build();

        GroupEntity currentGroup = groupOpt.get();
        UserEntity currentUser = userOpt.get();

        if(currentGroup.getElderUsers().contains(currentUser))
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

        if(!currentGroup.getMembers().contains(currentUser)) {
            currentUser.addRole("GROUP_MEMBER_" + currentGroup.getId());
            currentGroup.addMember(userService.save(currentUser));
            currentUser.addMemberIn(currentGroup);
            userService.save(currentUser);
        }
        currentUser.addRole("GROUP_ELDER_" + currentGroup.getId());
        currentGroup.addElderUser(userService.save(currentUser));
        currentUser.addElderInGroup(currentGroup);
        userService.save(currentUser);


        return ResponseEntity.ok(mapper.mapTo(groupService.save(currentGroup)));
    }

    @PreAuthorize("hasAuthority('GROUP_ADMIN_' + #groupId)")
    @PatchMapping("/change_admin/{groupId}")
    public ResponseEntity<GroupDto> changeAdmin(@PathVariable("groupId") Long groupId,
                                                @RequestParam("userId") Long userId){
        Optional<GroupEntity> groupOpt = groupService.getById(groupId);
        Optional<UserEntity> userOpt = userService.getUserById(userId);

        if(groupOpt.isEmpty() || userOpt.isEmpty())
            return ResponseEntity.notFound().build();

        GroupEntity currentGroup = groupOpt.get();
        UserEntity currentUser = userOpt.get();
        UserEntity currentAdmin = userService
                .findByEmail(currentGroup.getAdminUser().getEmail()).get();

        if(currentGroup.getAdminUser().equals(currentUser)
                || !currentGroup.getMembers().contains(currentUser))
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();

        currentAdmin.removeRole("GROUP_ADMIN_" + currentGroup.getId());
        userService.save(currentAdmin);

        currentUser.addRole("GROUP_ADMIN_" + currentGroup.getId());

        currentUser.addRole("GROUP_ELDER_" + currentGroup.getId());

        userService.save(currentUser);
        currentGroup.addElderUser(currentUser);
        currentGroup.addMember(currentUser);

        currentGroup.setAdminUser(currentUser);
        
        return ResponseEntity.ok(mapper.mapTo(groupService.save(currentGroup)));
    }

    @PreAuthorize("hasAuthority('GROUP_ADMIN_' + #groupId)")
    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId){
        Optional<GroupEntity> groupOpt = groupService.getById(groupId);
        if(groupOpt.isEmpty())
            return ResponseEntity.notFound().build();
        var group = groupOpt.get();
        Set<UserEntity> members = group.getMembers();
        members.stream().map(a -> userService.findByEmail(a.getEmail()).get())
                .forEach(a -> {
                    a.removeRole("GROUP_MEMBER_" + group.getId());
                    if(a.getRoles().contains("GROUP_ELDER_" + group.getId()))
                        a.removeRole("GROUP_ELDER_"+group.getId());
                });

        userService.saveAll(members);
        var admin = userService.getUserById(group.getAdminUser().getId()).get();
        admin.removeRole("GROUP_ADMIN_" + group.getId());
        userService.save(admin);
        groupService.deleteById(groupId);
        return ResponseEntity.ok(group.getName());
    }

    @PreAuthorize("hasAuthority('GROUP_MEMBER_') + #groupId")
    @GetMapping("/members_books")
    public Page<BookDto> getMemberBooks(@RequestParam("groupId") Long groupId, Pageable pageable){
        return userService
                .getAllBooks(groupService.getById(groupId).get().getMembers(), pageable)
                .map(bookMapper::mapTo);
    }
}
