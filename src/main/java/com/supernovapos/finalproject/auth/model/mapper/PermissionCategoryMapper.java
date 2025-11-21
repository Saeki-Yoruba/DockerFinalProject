package com.supernovapos.finalproject.auth.model.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.supernovapos.finalproject.auth.model.dto.PermissionCategoryDto;
import com.supernovapos.finalproject.auth.model.dto.PermissionDto;
import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.PermissionCategory;

@Mapper(componentModel = "spring")
public interface PermissionCategoryMapper {

    PermissionCategoryDto toDto(PermissionCategory entity);

    List<PermissionCategoryDto> toDtoList(List<PermissionCategory> entities);

    PermissionDto toDto(Permission entity);

    List<PermissionDto> toPermissionDtoList(List<Permission> entities);
}
