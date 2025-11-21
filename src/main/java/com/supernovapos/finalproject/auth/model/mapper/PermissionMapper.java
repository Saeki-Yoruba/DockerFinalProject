package com.supernovapos.finalproject.auth.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.supernovapos.finalproject.auth.model.dto.PermissionCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.PermissionDto;
import com.supernovapos.finalproject.auth.model.dto.PermissionResponse;
import com.supernovapos.finalproject.auth.model.dto.PermissionUpdateRequest;
import com.supernovapos.finalproject.auth.model.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @Mapping(source = "category.categoryName", target = "categoryName")
    PermissionResponse toResponse(Permission permission);  // for API response with category

    PermissionDto toDto(Permission permission);  // for simple DTO (like binding)

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "category", ignore = true)
    Permission toEntity(PermissionCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(PermissionUpdateRequest dto, @MappingTarget Permission entity);
}