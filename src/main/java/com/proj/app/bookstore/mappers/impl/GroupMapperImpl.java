package com.proj.app.bookstore.mappers.impl;

import com.proj.app.bookstore.domain.dto.GroupDto;
import com.proj.app.bookstore.domain.dto.UserDto;
import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.repositories.UserRepository;
import com.proj.app.bookstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupMapperImpl implements Mapper<GroupEntity, GroupDto> {
    private final ModelMapper mapper;
    private final Mapper<UserEntity, UserDto> userMapper;
    private final UserService userService;
    @Override
    public GroupDto mapTo(GroupEntity groupEntity) {
        GroupDto mapped = mapper.map(groupEntity, GroupDto.class);
        mapped.setAdminUser(userService.findByEmail(groupEntity.getAdminUser().getEmail())
                .get().getEmail());
        mapped.setElderUsers(groupEntity.getElderUsers().stream()
                .map(a -> userService.findByEmail(a.getEmail()).get().getEmail())
                .collect(Collectors.toSet()));
        mapped.setMembers(groupEntity.getMembers().stream()
                .map(a -> userService.findByEmail(a.getEmail()).get().getEmail())
                .collect(Collectors.toSet()));

        return mapped;
    }

    @Override
    public GroupEntity mapFrom(GroupDto groupDto) {
        return mapper.map(groupDto, GroupEntity.class);
    }
}
