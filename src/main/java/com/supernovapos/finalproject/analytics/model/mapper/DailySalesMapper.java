package com.supernovapos.finalproject.analytics.model.mapper;

import org.mapstruct.Mapper;

import com.supernovapos.finalproject.analytics.model.dto.DailySalesResponse;
import com.supernovapos.finalproject.analytics.model.entity.DailySalesView;

@Mapper(componentModel = "spring")
public interface DailySalesMapper {
    DailySalesResponse toDto(DailySalesView entity);
}
