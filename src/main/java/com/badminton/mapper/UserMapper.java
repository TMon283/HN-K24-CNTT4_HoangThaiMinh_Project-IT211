package com.badminton.mapper;

import com.badminton.constant.RoleType;
import com.badminton.dto.response.UserResponse;
import com.badminton.entity.Role;
import com.badminton.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleTypes")
    UserResponse toResponse(User user);

    @Named("rolesToRoleTypes")
    default Set<RoleType> rolesToRoleTypes(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
