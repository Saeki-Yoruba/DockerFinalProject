package com.supernovapos.finalproject.auth.model.mapper;

import com.supernovapos.finalproject.auth.model.dto.RoleResponse;
import com.supernovapos.finalproject.auth.model.entity.Role;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toDto(Role role);
}
