package com.supernovapos.finalproject.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.supernovapos.finalproject.store.model.Store;
import com.supernovapos.finalproject.store.model.StoreAdminResponseDto;
import com.supernovapos.finalproject.store.model.StoreResponseDto;
import com.supernovapos.finalproject.store.model.StoreUpdateDto;

@Mapper(componentModel = "spring")
public interface StoreMapper {
	StoreResponseDto toCustomerDto(Store store);

	StoreAdminResponseDto toAdminDto(Store store);

	// 更新時不允許改動 id / 建立時間 / 更新時間
	// 但允許前端修改其他所有欄位（包含 isActive）
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateStoreFromDto(StoreUpdateDto dto, @MappingTarget Store store);
	
	// 店長更新：不可動 isActive
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateStoreWithoutStatus(StoreUpdateDto dto, @MappingTarget Store store);
}