package com.supernovapos.finalproject.analytics.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.data.jpa.domain.Specification;

import com.supernovapos.finalproject.analytics.model.entity.GroupOrdersView;

public class GroupOrdersSpecifications {

    // 查單日
    public static Specification<GroupOrdersView> onDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return (root, query, cb) -> cb.between(root.get("createdAt"), start, end);
    }

    // 查某月
    public static Specification<GroupOrdersView> inMonth(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return (root, query, cb) ->
            cb.between(root.get("createdAt"), start.atStartOfDay(), end.atTime(23, 59, 59));
    }

    // 查某年
    public static Specification<GroupOrdersView> inYear(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return (root, query, cb) ->
            cb.between(root.get("createdAt"), start.atStartOfDay(), end.atTime(23, 59, 59));
    }

    // 動態入口
    public static Specification<GroupOrdersView> byMode(String mode, String base) {
        return (root, query, cb) -> {
            switch (mode) {
                case "date" -> {
                    LocalDate date = LocalDate.parse(base);
                    return onDate(date).toPredicate(root, query, cb);
                }
                case "month" -> {
                    YearMonth ym = YearMonth.parse(base);
                    return inMonth(ym).toPredicate(root, query, cb);
                }
                case "year" -> {
                    int year = Integer.parseInt(base);
                    return inYear(year).toPredicate(root, query, cb);
                }
                default -> throw new IllegalArgumentException("不支援的 mode: " + mode);
            }
        };
    }
}

