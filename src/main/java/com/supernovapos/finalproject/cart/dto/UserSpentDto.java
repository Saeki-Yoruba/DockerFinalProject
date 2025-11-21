package com.supernovapos.finalproject.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSpentDto {
    private String nickname;
    private int totalSpent;
}
