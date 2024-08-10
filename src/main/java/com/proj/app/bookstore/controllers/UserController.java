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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
import java.util.Set;


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
        return userService.findById(id).map(
                user -> ResponseEntity.ok(mapper.mapTo(user))
        ).orElse(
                ResponseEntity.notFound().build()
        );
    }

    /**
     * Deletes the user with email specified by Json Web Token <br>
     * <br>
     * This method performs safe deletion. Removing the user from all the groups they were part off,
     * assigning groups where they were admin new random admin and removing from {@code DB}
     * @return The user we want to delete.
     */
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
        return ResponseEntity.ok(mapper.mapTo(removed));
    }

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
}
