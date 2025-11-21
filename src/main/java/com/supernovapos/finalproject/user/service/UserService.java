package com.supernovapos.finalproject.user.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.supernovapos.finalproject.user.model.dto.UserOrderResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserRegisterDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserUpdateDto;
import com.supernovapos.finalproject.user.model.entity.User;

public interface UserService {

	UserResponseDto registerUser(UserRegisterDto dto);

	void resendVerificationEmail(String username);

	void verifyAccount(String token);

	UserResponseDto updateUser(Long id, UserUpdateDto dto);

	UserResponseDto updateUserStatus(Long id, boolean isAvailable);

	UserResponseDto deactivateUser(Long id);

	UserResponseDto getCurrentUser(User user);

	Optional<UserResponseDto> findUserById(Long id);

	Page<UserOrderResponseDto> getMyOrders(User user,Pageable pageable);
		
	User findByEmailOrPhoneOrThrow(String input);

	Page<UserResponseDto> findAllUsers(Pageable pageable);


}