package com.supernovapos.finalproject.analytics.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.supernovapos.finalproject.analytics.model.dto.ProductSaleResponse;
import com.supernovapos.finalproject.analytics.model.entity.ProductSalesView;

@Mapper(componentModel = "spring")
public interface ProductSaleMapper {
    ProductSaleMapper INSTANCE = Mappers.getMapper(ProductSaleMapper.class);

    ProductSaleResponse toDto(ProductSalesView entity);
}