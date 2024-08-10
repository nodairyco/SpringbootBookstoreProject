package com.proj.app.bookstore.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserDto {
    private Long id;

    @JsonProperty("user_name")
    private String name;

    private String email;

    private String password;

    private Set<String> roles;

    @Override
    public int hashCode(){
        return Objects.hash(id, name, email, password);
    }
    @Override
    public String toString(){
        return id + name + email;
    }
}
