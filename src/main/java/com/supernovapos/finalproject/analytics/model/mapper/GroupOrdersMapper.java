package com.supernovapos.finalproject.analytics.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GroupOrdersMapper {
	GroupOrdersMapper INSTANCE = Mappers.getMapper(GroupOrdersMapper.class);

}
