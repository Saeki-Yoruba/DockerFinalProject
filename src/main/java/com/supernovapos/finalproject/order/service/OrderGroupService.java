package com.supernovapos.finalproject.order.service;

import java.time.LocalDate;
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
import com.supernovapos.finalproject.order.dto.OrderGroupDetailDto;
import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;
import com.supernovapos.finalproject.table.model.RestaurantTable;
import com.supernovapos.finalproject.table.repository.RestaurantTableRepository;

//1. OrderGroupService - 訂單組管理服務

@Service
@Transactional
public class OrderGroupService {

	@Autowired
	private OrderGroupRepository orderGroupRepository;

	@Autowired
	private OrdersRepository ordersRepository;

	@Autowired
	private RestaurantTableRepository restaurantTableRepository;

//	創建新的點餐會話(產生QR CODE)
	public OrderGroup createNewSession(Integer tableId) {
		// 檢查桌子是否存在
		Optional<RestaurantTable> tableOpt = restaurantTableRepository.findByTableId(tableId);
		if (!tableOpt.isPresent()) {
			throw new ResourceNotFoundException("桌號 " + tableId + " 不存在");
		}
		RestaurantTable table = tableOpt.get();

		// 檢查桌子是否可用
		if (!table.isTableAvailable()) {
			throw new ConflictException("桌號 " + tableId + " 目前不可使用，狀態：" + table.getIsAvailable());
		}

		// 檢查桌子是否已有進行中的訂單
		Optional<OrderGroup> existingOrderGroup = orderGroupRepository
				.findActiveOrderGroupByTableId(table.getId().intValue());
		if (existingOrderGroup.isPresent()) {
			throw new ConflictException("桌號 " + tableId + " 已有進行中的訂單會話");
		}

		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setTable(table);
		orderGroup.setTotalAmount(0);
		orderGroup.setStatus(true); // 活躍狀態
		orderGroup.setHasOrder(false); // 尚未有人提交訂單

		OrderGroup savedGroup = orderGroupRepository.save(orderGroup);

//		// 更新桌子狀態為用餐
//		table.setDining();
//		restaurantTableRepository.save(table);
		// QR Code URL 就是: https://your-domain.com/order/{savedGroup.getId()}
		return savedGroup;
	}

	// 根據 QR Code 掃描結果查找訂單群組
	public OrderGroup findByQrCode(UUID orderGroupId) {
	    Optional<OrderGroup> findActiveOrderGroup = orderGroupRepository.findActiveOrderGroupWithTable(orderGroupId);
	    if (!findActiveOrderGroup.isPresent()) {
	        throw new ResourceNotFoundException("找不到對應的訂單群組或QrCode過期");
	    }
	    return findActiveOrderGroup.get();
	}

//	檢查是否可以提交首次訂單
	public boolean canSubmitFirstOrder(UUID groupId) {
		OrderGroup group = findByQrCode(groupId);
		return group.getStatus() && !group.getHasOrder();
	}

//	檢查是否可以加點
	public boolean canAddOrder(UUID groupId) {
		OrderGroup group = findByQrCode(groupId);
		return group.getStatus() && group.getHasOrder();
	}

//	完成訂單群組(結帳)
	public void completeOrderGroup(UUID groupId) {
		OrderGroup group = findByQrCode(groupId);
		group.setStatus(false); // 設為完成
		group.setCompletedAt(LocalDateTime.now());

		// 重新計算最終總金額
		Integer finalTotal = ordersRepository.sumTotalAmountByGroupId(groupId);
		group.setTotalAmount(finalTotal);
		orderGroupRepository.save(group);

		// 更新桌子狀態為清潔
//		RestaurantTable table = group.getTable();
//		table.setCleaning();
//		restaurantTableRepository.save(table);
	}

//	取得今日業績
	public Integer getTodayTotalRevenue() {
		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		return orderGroupRepository.getTodayTotalRevenue(startOfDay, endOfDay);
	}

//	取得指定日期業績
	public Integer getRevenueByDate(LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		return orderGroupRepository.getTodayTotalRevenue(startOfDay, endOfDay);
	}

//	取得訂單群組詳細資訊(包含所有訂單)
	public OrderGroupDetailDto getOrderGroupDetail(UUID groupId) {
	    OrderGroup group = findByQrCode(groupId);
	    
	    // 使用新的查詢方法取得所有訂單（包含完整資料）
	    ordersRepository.findAllOrdersByGroupIdWithDetails(groupId);

	    OrderGroupDetailDto dto = new OrderGroupDetailDto();
	    dto.setOrderGroup(group);
	    dto.setCanSubmitFirstOrder(canSubmitFirstOrder(groupId));
	    dto.setCanAddOrder(canAddOrder(groupId));
	    return dto;
	}

//	取得指定日期的完成訂單列表
	public List<OrderGroup> getCompletedOdersByDate(LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		return orderGroupRepository.findCompletedOrdersByDate(startOfDay, endOfDay);
	}

//	取得所有進行中的訂單群組
	public List<OrderGroup> getActiveOrderGroups() {
		List<OrderGroup> allGroups = orderGroupRepository.findAll();
		List<OrderGroup> activeGroups = new ArrayList<>();

		for (OrderGroup group : allGroups) {
			if (group.getStatus()) {
				activeGroups.add(group);
			}
		}
		return activeGroups;
	}

//	根據桌子ID查詢進行中的訂單群組
	public OrderGroup getActiveOrderGroupByTableId(Integer tableId) {
		Optional<RestaurantTable> tableOpt = restaurantTableRepository.findByTableId(tableId);
		if (!tableOpt.isPresent()) {
			throw new ResourceNotFoundException("桌號 " + tableId + " 不存在");
		}

		Optional<OrderGroup> orderGroupOpt = orderGroupRepository
				.findActiveOrderGroupByTableId(tableOpt.get().getId().intValue());
		if (!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("桌號 " + tableId + " 沒有進行中的訂單");
		}

		return orderGroupOpt.get();
	}

//	檢查桌子是否有進行中的訂單
	public boolean hasActiveOrderByTableId(Integer tableId) {
		try {
			getActiveOrderGroupByTableId(tableId);
			return true;
		} catch (ResourceNotFoundException e) {
			return false;
		}
	}

//	強制結束訂單群組（用於異常情況處理）
	public void forceCompleteOrderGroup(UUID groupId) {
		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findById(groupId);
		if (!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單群組不存在");
		}

		OrderGroup group = orderGroupOpt.get();
		group.setStatus(false);
		group.setCompletedAt(LocalDateTime.now());

		Integer finalTotal = ordersRepository.sumTotalAmountByGroupId(groupId);
		group.setTotalAmount(finalTotal);
		orderGroupRepository.save(group);

		// 更新桌子狀態
//		RestaurantTable table = group.getTable();
//		if (table != null) {
//			table.setCleaning();
//			restaurantTableRepository.save(table);
		}
	

//	取得訂單群組的桌子資訊
	public RestaurantTable getTableByOrderGroupId(UUID groupId) {
		OrderGroup group = findByQrCode(groupId);
		return group.getTable();
	}
}
