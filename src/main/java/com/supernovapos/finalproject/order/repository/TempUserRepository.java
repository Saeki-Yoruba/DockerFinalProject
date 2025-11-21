package com.supernovapos.finalproject.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.order.model.TempUser;

public interface TempUserRepository extends JpaRepository<TempUser, UUID> {
	
// 	修改：根據暱稱和訂單組查詢用戶
    @Query("SELECT tu FROM TempUser tu WHERE tu.nickname = :nickname AND tu.orderGroup.id = :orderGroupId")
    Optional<TempUser> findByNicknameAndOrderGroupId(@Param("nickname") String nickname, @Param("orderGroupId") UUID orderGroupId);
	
//	根據暱稱查詢用戶(避免重複暱稱)
	Optional<TempUser> findByNickname(String nickname);

// 	根據訂單組查詢所有臨時用戶
    @Query("SELECT tu FROM TempUser tu WHERE tu.orderGroup.id = :orderGroupId")
    List<TempUser> findByOrderGroupId(@Param("orderGroupId") UUID orderGroupId);
	
//	查詢指定時間內活躍的用戶
	@Query("select tu from TempUser tu where tu.createdAt >= :since")
	List<TempUser> findActiveUsersSince(@Param("since") LocalDateTime since);
	
//	清理舊的臨時用戶 (定期清理任務用)
	@Modifying
	@Query("delete from TempUser tu where tu.createdAt < :before and tu.isRegister = false")
	int deleteOldTempUsers(@Param("before") LocalDateTime before);
	
// 	清理特定訂單組的臨時用戶	
	@Modifying
    @Query("DELETE FROM TempUser tu WHERE tu.orderGroup.id = :orderGroupId")
    int deleteByOrderGroupId(@Param("orderGroupId") UUID orderGroupId);

}
