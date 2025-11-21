package com.supernovapos.finalproject.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supernovapos.finalproject.auth.model.entity.VerificationToken;
import com.supernovapos.finalproject.user.model.entity.User;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
	
	/**
     * 依據驗證 token 查詢
     *
     * @param token 驗證用的 token 字串
     * @return Optional<VerificationToken>
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * 刪除指定使用者的驗證 token
     * - 常用於帳號啟用後清理舊 token
     *
     * @param user 使用者實體
     */
    void deleteByUser(User user);
    
}
