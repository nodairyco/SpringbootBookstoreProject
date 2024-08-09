package com.proj.app.bookstore.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GroupDto {
    private String name;
    private Long id;
    private String adminUser;
    private Set<String> elderUsers;
    private Set<String> members;
    @Override
    public int hashCode(){
        return Objects.hash(name, id);
    }
    @Override
    public String toString(){
        return id + name + adminUser;
    }
}
