package com.supernovapos.finalproject.store.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.store.mapper.StoreMapper;
import com.supernovapos.finalproject.store.model.Store;
import com.supernovapos.finalproject.store.model.StoreAdminResponseDto;
import com.supernovapos.finalproject.store.model.StoreResponseDto;
import com.supernovapos.finalproject.store.model.StoreUpdateDto;
import com.supernovapos.finalproject.store.repository.StoreRepository;
import com.supernovapos.finalproject.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final StoreMapper storeMapper;

	/**
	 * 取得前台顧客用的商店資訊
	 */
	@Override
	@Transactional(readOnly = true)
	public StoreResponseDto getStoreForCustomer() {
		Store store = storeRepository.findTopByOrderByIdAsc()
				.orElseThrow(() -> new ResourceNotFoundException("找不到商店資訊"));
		return storeMapper.toCustomerDto(store);
	}

	/**
	 * 取得餐廳是否啟用
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	public boolean isStoreActive() {
		return storeRepository.findTopByOrderByIdAsc()
				.map(Store::getIsActive)
				.map(Boolean::booleanValue)
				.orElseThrow(() -> new ResourceNotFoundException("找不到商店資訊"));
	}

	/**
	 * 取得後台管理員/店長用的商店資訊
	 */
	@Override
	@Transactional(readOnly = true)
	public StoreAdminResponseDto getAdminStore() {
		Store store = storeRepository.findTopByOrderByIdAsc()
				.orElseThrow(() -> new ResourceNotFoundException("找不到商店資訊"));
		return storeMapper.toAdminDto(store);
	}

	/**
	 * 更新商店資訊
	 * 
	 * @param dto               更新資料
	 * @param allowUpdateStatus 是否允許更新 isActive
	 */
	@Override
	@Transactional
	public StoreAdminResponseDto updateStore(StoreUpdateDto dto, boolean allowUpdateStatus) {
		Store store = storeRepository.findTopByOrderByIdAsc()
				.orElseThrow(() -> new ResourceNotFoundException("找不到商店資訊"));

		if (allowUpdateStatus) {
			// ADMIN 更新：可改 isActive
			storeMapper.updateStoreFromDto(dto, store);
		} else {
			// OWNER 更新：不可改 isActive
			storeMapper.updateStoreWithoutStatus(dto, store);
		}

		Store updated = storeRepository.save(store);
		return storeMapper.toAdminDto(updated);
	}
}
