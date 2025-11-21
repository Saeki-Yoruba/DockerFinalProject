package com.supernovapos.finalproject.user.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.supernovapos.finalproject.user.model.dto.BindResponseDto;

@Mapper(componentModel = "spring")
public interface BindResponseMapper {

    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "userEmail", ignore = true) // 綁定時不用
    BindResponseDto toBindResponse(String provider, String message, String uid, String email, String nickname);

    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "message", constant = "帳號已解除綁定")
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "userEmail", source = "userEmail")
    BindResponseDto toUnbindResponse(String provider, String userEmail);
}
