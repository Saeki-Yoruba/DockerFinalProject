package com.supernovapos.finalproject.auth.model.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.supernovapos.finalproject.auth.model.dto.AuthResponse;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.user.model.entity.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", ignore = true) // token 登入時才塞
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "point", target = "point")
    @Mapping(source = "userRoles", target = "roles")
    AuthResponse toAuthResponse(User user);

    // userRoles → List<String>
    default List<String> mapRoles(Set<UserRole> userRoles) {
        return userRoles.stream()
                .map(ur -> ur.getRole().getCode())
                .toList();
    }
}