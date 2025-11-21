package com.supernovapos.finalproject.analytics.model.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.supernovapos.finalproject.analytics.model.dto.UserSpendingDto;
import com.supernovapos.finalproject.analytics.model.entity.UserSpendingView;

@Mapper(componentModel = "spring")
public interface UserSpendingMapper {

    @Mapping(target = "avgSpent", expression = "java(calcAvgSpent(entity))")
    UserSpendingDto toDto(UserSpendingView entity);

    List<UserSpendingDto> toDtoList(List<UserSpendingView> entities);

    default BigDecimal calcAvgSpent(UserSpendingView entity) {
        if (entity.getOrderCount() != null && entity.getOrderCount() > 0 && entity.getTotalSpent() != null) {
            return entity.getTotalSpent().divide(
                    BigDecimal.valueOf(entity.getOrderCount()),
                    2,
                    RoundingMode.HALF_UP
            );
        }
        return BigDecimal.ZERO;
    }
}
