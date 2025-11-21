package com.supernovapos.finalproject.cart.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.cart.dto.CartItemDto;
import com.supernovapos.finalproject.cart.dto.CartOrderItemDto;
import com.supernovapos.finalproject.cart.dto.OrderGroupCartDto;
import com.supernovapos.finalproject.cart.dto.ProductSimpleDto;
import com.supernovapos.finalproject.cart.dto.UserSpentDto;
import com.supernovapos.finalproject.cart.repo.OrderRepository;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final OrderGroupRepository orderGroupRepository;
    private final OrderRepository orderRepository;

    public OrderGroupCartDto getOrderGroupCartStatus(UUID groupId) {
        OrderGroup orderGroup = orderGroupRepository.findActiveOrderGroup(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("訂單組不存在或已過期"));

        // 尚未提交的購物車
        List<Orders> cartOrders = orderRepository.findUnsubmittedByGroupId(groupId);
        List<CartItemDto> cartItems = mapOrdersToCartItems(cartOrders);

        // 已提交的訂單
        List<Orders> submittedOrders = orderRepository.findSubmittedByGroupId(groupId);
        List<CartItemDto> submittedItems = mapOrdersToCartItems(submittedOrders);

        // 計算總額
        int totalCartAmount = cartItems.stream().mapToInt(CartItemDto::getTotalAmount).sum();
        int totalSubmittedAmount = submittedItems.stream().mapToInt(CartItemDto::getTotalAmount).sum();
        int grandTotal = totalCartAmount + totalSubmittedAmount;

        // 回傳 DTO
        OrderGroupCartDto dto = new OrderGroupCartDto();
        dto.setGroupId(orderGroup.getId());
        dto.setTableId(orderGroup.getTable().getTableId());
        dto.setCartItems(cartItems);
        dto.setSubmittedOrders(submittedItems);
        dto.setGrandTotal(grandTotal);
        dto.setCanSubmitFirstOrder(!orderGroup.getHasOrder() && orderGroup.getStatus());
        dto.setCanAddOrder(orderGroup.getHasOrder() && orderGroup.getStatus());

        return dto;
    }

    public List<UserSpentDto> getOrderGroupSummary(UUID groupId) {
        // 找群組，不限定 status
        OrderGroup orderGroup = orderGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("訂單組不存在"));

        // 找出該群組所有「已提交」的訂單
        List<Orders> submittedOrders = orderRepository.findSubmittedByGroupId(groupId);

        // 按使用者分組並計算總花費
        Map<String, Integer> userSpentMap = submittedOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> {
                            if (o.getUser() != null) return o.getUser().getNickname();
                            if (o.getTempUser() != null) return o.getTempUser().getNickname();
                            return "匿名";
                        },
                        Collectors.summingInt(Orders::getTotalAmount)
                ));

        // 轉成 DTO 清單
        return userSpentMap.entrySet().stream()
                .map(e -> new UserSpentDto(e.getKey(), e.getValue()))
                .toList();
    }
    
    private List<CartItemDto> mapOrdersToCartItems(List<Orders> orders) {
        return orders.stream().map(order -> {
            CartItemDto dto = new CartItemDto();
            dto.setOrderId(order.getId());
            dto.setUserId(order.getTempUser() != null ? order.getTempUser().getId().toString()
                    : order.getUser() != null ? order.getUser().getId().toString() : null);
            dto.setUserType(order.getTempUser() != null ? "TEMP" : "REGISTERED");
            dto.setUserNickname(order.getTempUser() != null ? order.getTempUser().getNickname()
                    : order.getUser() != null ? order.getUser().getNickname() : "匿名");
            dto.setTotalAmount(order.getTotalAmount());
            dto.setNote(order.getNote());
            dto.setCreatedAt(order.getCreatedAt());

            // 映射 orderItems
            List<CartOrderItemDto> itemDtos = order.getOrderItems().stream().map(oi -> {
                CartOrderItemDto itemDto = new CartOrderItemDto();
                itemDto.setId(oi.getId());
                itemDto.setQuantity(oi.getQuantity());
                itemDto.setUnitPrice(oi.getUnitPrice());
                itemDto.setSubtotal(oi.getSubtotal()); // 加入小計
                itemDto.setNote(oi.getNote());
                itemDto.setCreatedAt(oi.getCreatedAt());

                // 包裝 products 物件
                ProductSimpleDto productDto = new ProductSimpleDto();
                productDto.setId(oi.getProducts().getId());
                productDto.setName(oi.getProducts().getName());
                productDto.setImage(oi.getProducts().getImage());
                productDto.setPrice(oi.getProducts().getPrice());
                itemDto.setProducts(productDto);

                return itemDto;
            }).toList();

            dto.setOrderItems(itemDtos);
            return dto;
        }).toList();
    }

}

