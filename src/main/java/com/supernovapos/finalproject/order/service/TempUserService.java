package com.supernovapos.finalproject.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.order.dto.OrderGroupUserStatsDto;
import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.TempUser;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.TempUserRepository;

// ===== 3. TempUserService - 臨時用戶管理服務 =====

@Service
@Transactional
public class TempUserService {
	
	@Autowired
	private TempUserRepository tempUserRepository;
	
	@Autowired
	private OrderGroupRepository orderGroupRepository;
	
//	在指定的訂單群組中創建新的臨時用戶
	public TempUser createTempUserForOrderGroup(String nickname, UUID orderGroupId) {
		// 檢查訂單群組是否存在且活躍
		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findActiveOrderGroup(orderGroupId);
		if(!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單群組不存在或已過期");
		}
		OrderGroup orderGroup = orderGroupOpt.get();
		
		// 檢查在此訂單群組內暱稱是否已存在
		Optional<TempUser> existingUser = tempUserRepository.findByNicknameAndOrderGroupId(nickname, orderGroupId);
		if(existingUser.isPresent()) {
			throw new ConflictException("此桌已有人使用暱稱 '" + nickname + "'，請換一個暱稱");
		}
		
		TempUser tempUser = new TempUser();
		tempUser.setNickname(nickname);
		tempUser.setIsRegister(false);
		tempUser.setOrderGroup(orderGroup);
		
		return tempUserRepository.save(tempUser);
		
	}

// 	檢查暱稱在指定訂單群組內是否可用
	public boolean isNicknameAvailableInOrderGroup(String nickname, UUID orderGroupId) {
		Optional<TempUser> existingUser = tempUserRepository.findByNicknameAndOrderGroupId(nickname, orderGroupId);
		return !existingUser.isPresent();
	}
	
//	根據 ID 查詢臨時用戶
	public TempUser findById(UUID tempUserId) {
		Optional<TempUser> tempUserOpt = tempUserRepository.findById(tempUserId);
		if(!tempUserOpt.isPresent()) {
			throw new ResourceNotFoundException("臨時用戶不存在");
		}
		return tempUserOpt.get();
	}
	
// 在指定訂單組中根據暱稱查詢臨時用戶
	public TempUser findByNicknameInOrderGroup(String nickname, UUID orderGroupId) {
		Optional<TempUser> tempUserOpt = tempUserRepository.findByNicknameAndOrderGroupId(nickname, orderGroupId);
		if(!tempUserOpt.isPresent()) {
			throw new ResourceNotFoundException("在此桌找不到暱稱為 '" + nickname + "' 的用戶");
		}
		return tempUserOpt.get();
	}

//	更新用戶暱稱（在同一訂單組內檢查重複）
	public TempUser updateNickname(String newNickname, UUID tempUserId) {
		TempUser tempUser = findById(tempUserId);
		UUID orderGroupId = tempUser.getOrderGroup().getId();
		
		// 檢查新暱稱在此訂單組內是否已被使用
		Optional<TempUser> existingUser = tempUserRepository.findByNicknameAndOrderGroupId(newNickname, orderGroupId);
		if(existingUser.isPresent() && !existingUser.get().getId().equals(tempUserId)) {
			throw new ConflictException("此桌已有人使用暱稱 '" + newNickname + "'");
		}
		tempUser.setNickname(newNickname);
		return tempUserRepository.save(tempUser);
	}
	
//	取得指定訂單群組的所有臨時用戶
	public List<TempUser> getTempUsersByOrderGroup(UUID orderGroupId){
		return tempUserRepository.findByOrderGroupId(orderGroupId);
	}
	
//	取得所有臨時用戶
	public List<TempUser> getAllTempUsers(){
		return tempUserRepository.findAll();
	}

//	取得活躍用戶列表
	public List<TempUser> getActiveUsers(int hourAgo){
		LocalDateTime since = LocalDateTime.now().minusHours(hourAgo);
		return tempUserRepository.findActiveUsersSince(since);
	}
	
// 清理舊的臨時用戶
	public int cleanupOldTempUsers(int daysOld) {
		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
		return tempUserRepository.deleteOldTempUsers(cutoffDate);
	}
	
// 清理指定訂單組的所有臨時用戶
	public int cleanupTempUsersByOrderGroup(UUID orderGroupId) {
		return tempUserRepository.deleteByOrderGroupId(orderGroupId);
	}
	
//	刪除臨時用戶
	public void deleteTempUser(UUID tempUserId) {
		TempUser tempUser = findById(tempUserId);
		tempUserRepository.delete(tempUser);
	}
	
//	驗證臨時用戶是否屬於指定訂單組
	public boolean validateTempUserInOrderGroup(UUID tempUserId, UUID orderGroupId) {
		TempUser tempUser = findById(tempUserId);
		return tempUser.getOrderGroup().getId().equals(orderGroupId);
		
	}
	
//	取得訂單群組內的用戶統計
	public OrderGroupUserStatsDto getOrderGroupUserStats(UUID orderGroupId) {
		List<TempUser> tempUsers = getTempUsersByOrderGroup(orderGroupId);
		
		OrderGroupUserStatsDto stats = new OrderGroupUserStatsDto();
		stats.setOrderGroupId(orderGroupId);
		stats.setTotalTempUsers(tempUsers.size());
		stats.setTempUserNicknames(new ArrayList<String> ());
		
		for(TempUser tempUser : tempUsers) {
			stats.getTempUserNicknames().add(tempUser.getNickname());
		}
		
		return stats;
	}
}
