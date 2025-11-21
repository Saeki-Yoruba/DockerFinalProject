package com.supernovapos.finalproject.analytics.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.supernovapos.finalproject.analytics.model.entity.UserSpendingView;

public class UserSpendingSpecifications {

    public static Specification<UserSpendingView> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nickname")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
    }

    public static Specification<UserSpendingView> minSpent(BigDecimal minSpent) {
        return (root, query, cb) -> {
            if (minSpent == null) return null;
            return cb.greaterThanOrEqualTo(root.get("totalSpent"), minSpent);
        };
    }

    public static Specification<UserSpendingView> dateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) {
                return cb.between(root.get("lastOrderDate"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("lastOrderDate"), start);
            } else {
                return cb.lessThanOrEqualTo(root.get("lastOrderDate"), end);
            }
        };
    }
}
