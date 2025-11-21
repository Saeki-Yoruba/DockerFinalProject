package com.supernovapos.finalproject.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.analytics.model.dto.MonthlySpendingDto;
import com.supernovapos.finalproject.analytics.model.dto.UserSpendingDto;
import com.supernovapos.finalproject.analytics.model.dto.UserSpendingRequest;
import com.supernovapos.finalproject.analytics.model.dto.UserSpendingSummaryDto;
import com.supernovapos.finalproject.analytics.model.entity.UserSpendingView;
import com.supernovapos.finalproject.analytics.model.mapper.UserSpendingMapper;
import com.supernovapos.finalproject.analytics.repository.UserSpendingRepository;
import com.supernovapos.finalproject.analytics.repository.UserSpendingSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSpendingService {

    private final UserSpendingRepository repository;
    private final UserSpendingMapper mapper;

    public Page<UserSpendingDto> search(UserSpendingRequest req) {

    	LocalDateTime start = req.getStartDate() != null ? req.getStartDate().atStartOfDay() : null;
    	LocalDateTime end = req.getEndDate() != null ? req.getEndDate().atTime(LocalTime.MAX) : null;
    	
    	Specification<UserSpendingView> spec = Specification.<UserSpendingView>where(null)
    	        .and(UserSpendingSpecifications.keyword(req.getKeyword()))
    	        .and(UserSpendingSpecifications.minSpent(req.getMinSpent()))
    	        .and(UserSpendingSpecifications.dateBetween(start, end));

        Pageable pageable = PageRequest.of(
                req.getPage(),
                req.getSize(),
                Sort.by(Sort.Direction.fromString(req.getDirection()), req.getSortBy())
        );

        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    public List<UserSpendingDto> findTopN(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalSpent"));
        return repository.findAll(pageable).map(mapper::toDto).getContent();
    }

    public UserSpendingSummaryDto getSummary(UserSpendingRequest req) {
    	LocalDateTime start = req.getStartDate() != null ? req.getStartDate().atStartOfDay() : null;
    	LocalDateTime end = req.getEndDate() != null ? req.getEndDate().atTime(LocalTime.MAX) : null;
    	
        Specification<UserSpendingView> spec = Specification.<UserSpendingView>where(null)
                .and(UserSpendingSpecifications.keyword(req.getKeyword()))
                .and(UserSpendingSpecifications.minSpent(req.getMinSpent()))
                .and(UserSpendingSpecifications.dateBetween(start, end));

        List<UserSpendingView> all = repository.findAll(spec);

        BigDecimal totalRevenue = all.stream()
                .map(UserSpendingView::getTotalSpent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalUsers = all.size();
        long activeUsers = all.stream()
                .filter(u -> u.getLastOrderDate() != null &&
                             u.getLastOrderDate().isAfter(LocalDateTime.now().minusMonths(1)))
                .count();
        BigDecimal avgOrderValue = totalUsers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new UserSpendingSummaryDto(totalUsers, totalRevenue, avgOrderValue, activeUsers);

    }
    
    public List<MonthlySpendingDto> getMonthlyTrend(UserSpendingRequest req) {
    	LocalDateTime start = req.getStartDate() != null ? req.getStartDate().atStartOfDay() : null;
    	LocalDateTime end = req.getEndDate() != null ? req.getEndDate().atTime(LocalTime.MAX) : null;
    	
        Specification<UserSpendingView> spec = Specification.<UserSpendingView>where(null)
                .and(UserSpendingSpecifications.keyword(req.getKeyword()))
                .and(UserSpendingSpecifications.minSpent(req.getMinSpent()))
                .and(UserSpendingSpecifications.dateBetween(start, end));

        List<UserSpendingView> all = repository.findAll(spec);

        // 依照年月分組
        Map<YearMonth, BigDecimal> grouped = all.stream()
                .filter(u -> u.getLastOrderDate() != null && u.getTotalSpent() != null)
                .collect(Collectors.groupingBy(
                        u -> YearMonth.from(u.getLastOrderDate()),
                        Collectors.mapping(UserSpendingView::getTotalSpent,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // 轉成 DTO list
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlySpendingDto(
                        e.getKey().toString(), // e.g. "2025-09"
                        e.getValue()
                ))
                .toList();
    }

}

