package com.supernovapos.finalproject.user.model.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.user.model.dto.StaffRegisterDto;
import com.supernovapos.finalproject.user.model.dto.StaffUpdateDto;
import com.supernovapos.finalproject.user.model.dto.UserRegisterDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserUpdateDto;
import com.supernovapos.finalproject.user.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ---------------- User ----------------

    // 註冊用：DTO → Entity
    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "googleUid", ignore = true)
    @Mapping(target = "lineUid", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "birthdate", ignore = true)
    @Mapping(target = "invoiceCarrier", ignore = true)
    @Mapping(target = "point", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    User toEntity(UserRegisterDto dto);

    // Entity → DTO
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "isActive", target = "active")
    @Mapping(source = "userRoles", target = "roles")
    UserResponseDto toDto(User user);

    // Entity List → DTO List
    List<UserResponseDto> toDtoList(List<User> users);

    // Entity Page → DTO Page
    default Page<UserResponseDto> toDtoPage(Page<User> users) {
        return users.map(this::toDto);
    }

    // userRoles → List<String> (取角色代碼)
    default List<String> mapRoles(Set<UserRole> userRoles) {
        return userRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
    }

    // 更新用（UserUpdateDto → Entity）
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User user);

    // ---------------- Staff ----------------

    // 新增員工：DTO → Entity
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "googleUid", ignore = true)
    @Mapping(target = "lineUid", ignore = true)
    @Mapping(target = "invoiceCarrier", ignore = true)
    @Mapping(target = "point", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    User toEntity(StaffRegisterDto dto);

    // 修改員工（部分欄位允許更新）
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStaffFromDto(StaffUpdateDto dto, @MappingTarget User user);
}
