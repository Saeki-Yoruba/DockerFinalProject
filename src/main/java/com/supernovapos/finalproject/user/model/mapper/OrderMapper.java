package com.supernovapos.finalproject.user.model.mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.user.model.dto.UserOrderItemDto;
import com.supernovapos.finalproject.user.model.dto.UserOrderResponseDto;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderCode", expression = "java(generateOrderCode(order))")
    @Mapping(target = "status", expression = "java(order.getStatus() ? \"已付款\" : \"已取消\")")
    @Mapping(target = "items", source = "orderItems")
    UserOrderResponseDto toDto(Orders order);

    List<UserOrderResponseDto> toDtoList(List<Orders> orders);

    @Mapping(target = "productName", source = "products.name")
    @Mapping(target = "unitPrice", source = "unitPrice")
    UserOrderItemDto toDto(OrderItems item);

    // 生成訂單編號
    default String generateOrderCode(Orders order) {
        if (order.getCreatedAt() == null || order.getId() == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return "O" + order.getCreatedAt().format(formatter) + String.format("%04d", order.getId());
    }
}
