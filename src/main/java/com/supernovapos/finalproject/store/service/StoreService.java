package com.supernovapos.finalproject.store.service;

import com.supernovapos.finalproject.store.model.StoreAdminResponseDto;
import com.supernovapos.finalproject.store.model.StoreResponseDto;
import com.supernovapos.finalproject.store.model.StoreUpdateDto;

public interface StoreService {

	/**
	 * 取得前台顧客用的商店資訊
	 */
	StoreResponseDto getStoreForCustomer();

	/**
	 * 取得餐廳是否啟用
	 */
	boolean isStoreActive();

	/**
	 * 取得後台管理員/店長用的商店資訊
	 */
	StoreAdminResponseDto getAdminStore();

	/**
	 * 更新商店資訊
	 * @param dto 更新資料
	 * @param allowUpdateStatus 是否允許更新 isActive
	 */
	StoreAdminResponseDto updateStore(StoreUpdateDto dto, boolean allowUpdateStatus);

}