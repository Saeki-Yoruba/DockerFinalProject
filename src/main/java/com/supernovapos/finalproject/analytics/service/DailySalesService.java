package com.supernovapos.finalproject.analytics.service;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.analytics.model.dto.DailySalesRequest;
import com.supernovapos.finalproject.analytics.model.dto.DailySalesResponse;
import com.supernovapos.finalproject.analytics.model.entity.DailySalesView;
import com.supernovapos.finalproject.analytics.model.mapper.DailySalesMapper;
import com.supernovapos.finalproject.analytics.repository.DailySalesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailySalesService {

    private final DailySalesRepository dailySalesRepository;
    private final DailySalesMapper dailySalesMapper;

    public Page<DailySalesResponse> getDailySalesReport(DailySalesRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                Sort.by(
                        "ASC".equalsIgnoreCase(request.getSortOrder()) 
                            ? Sort.Direction.ASC 
                            : Sort.Direction.DESC,
                        request.getSortBy() != null ? request.getSortBy() : "orderDate"
                )
        );

        Specification<DailySalesView> spec = switch (request.getMode()) {
            case "day"   -> buildDaySpec(request);
            case "range" -> buildRangeSpec(request);
            case "month" -> buildMonthSpec(request);
            case "monthRangeDay" -> buildMonthRangeDaySpec(request);
            case "year"  -> buildYearSpec(request);
            case "yearRangeMonth" -> buildYearRangeMonthSpec(request);
            default      -> throw new IllegalArgumentException("Unsupported mode: " + request.getMode());
        };

        return dailySalesRepository.findAll(spec, pageable)
                .map(dailySalesMapper::toDto);
    }

    // === 私有方法 ===
    private Specification<DailySalesView> buildDaySpec(DailySalesRequest request) {
        return (root, query, cb) -> {
            if (request.getDate() != null) {
                return cb.equal(root.get("orderDate"), LocalDate.parse(request.getDate()));
            }
            return cb.conjunction();
        };
    }

    private Specification<DailySalesView> buildRangeSpec(DailySalesRequest request) {
        return (root, query, cb) -> {
            if (request.getStartDate() != null && request.getEndDate() != null) {
                return cb.between(
                        root.get("orderDate"),
                        LocalDate.parse(request.getStartDate()),
                        LocalDate.parse(request.getEndDate())
                );
            }
            return cb.conjunction();
        };
    }

    private Specification<DailySalesView> buildMonthSpec(DailySalesRequest request) {
        return (root, query, cb) -> {
            if (request.getMonth() != null) {
                YearMonth ym = YearMonth.parse(request.getMonth()); // 格式: yyyy-MM
                return cb.between(
                        root.get("orderDate"),
                        ym.atDay(1),
                        ym.atEndOfMonth()
                );
            }
            return cb.conjunction();
        };
    }

    private Specification<DailySalesView> buildMonthRangeDaySpec(DailySalesRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            YearMonth start = YearMonth.parse(request.getStartDate());
            YearMonth end = YearMonth.parse(request.getEndDate());
            return (root, query, cb) -> cb.between(
                root.get("orderDate"),
                start.atDay(1),
                end.atEndOfMonth()
            );
        }
        return (root, query, cb) -> cb.conjunction();
    }
    
    private Specification<DailySalesView> buildYearSpec(DailySalesRequest request) {
        return (root, query, cb) -> {
            if (request.getYear() != null) {
                int year = Integer.parseInt(request.getYear());
                return cb.between(
                        root.get("orderDate"),
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31)
                );
            }
            return cb.conjunction();
        };
    }
    
    private Specification<DailySalesView> buildYearRangeMonthSpec(DailySalesRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            int startYear = Integer.parseInt(request.getStartDate());
            int endYear = Integer.parseInt(request.getEndDate());
            return (root, query, cb) -> cb.between(
                root.get("orderDate"),
                LocalDate.of(startYear, 1, 1),
                LocalDate.of(endYear, 12, 31)
            );
        }
        return (root, query, cb) -> cb.conjunction();
    }

}
