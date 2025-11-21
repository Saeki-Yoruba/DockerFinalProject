package com.supernovapos.finalproject.analytics.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.analytics.model.dto.RevenueTrendResponse;
import com.supernovapos.finalproject.analytics.model.entity.GroupOrdersView;
import com.supernovapos.finalproject.analytics.repository.GroupOrdersRepository;
import com.supernovapos.finalproject.analytics.repository.GroupOrdersSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final GroupOrdersRepository repo;

    public RevenueTrendResponse getRevenueTrend(String mode, String base) {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        if ("date".equalsIgnoreCase(mode)) {
            LocalDate baseDate = LocalDate.parse(base);
            for (int i = 6; i >= 0; i--) {
                LocalDate day = baseDate.minusDays(i);
                Specification<GroupOrdersView> spec = GroupOrdersSpecifications.onDate(day);
                List<GroupOrdersView> data = repo.findAll(spec);

                long revenue = data.stream().mapToLong(v -> v.getTotalRevenue() != null ? v.getTotalRevenue() : 0).sum();
                labels.add(day.format(DateTimeFormatter.ofPattern("MM-dd")));
                values.add(revenue);
            }
        } else if ("month".equalsIgnoreCase(mode)) {
            YearMonth baseMonth = YearMonth.parse(base);
            for (int i = 5; i >= 0; i--) {
                YearMonth ym = baseMonth.minusMonths(i);
                Specification<GroupOrdersView> spec = GroupOrdersSpecifications.inMonth(ym);
                List<GroupOrdersView> data = repo.findAll(spec);

                long revenue = data.stream().mapToLong(v -> v.getTotalRevenue() != null ? v.getTotalRevenue() : 0).sum();
                labels.add(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                values.add(revenue);
            }
        } else if ("year".equalsIgnoreCase(mode)) {
            int baseYear = Integer.parseInt(base);
            for (int i = 4; i >= 0; i--) {
                int year = baseYear - i;
                Specification<GroupOrdersView> spec = GroupOrdersSpecifications.inYear(year);
                List<GroupOrdersView> data = repo.findAll(spec);

                long revenue = data.stream().mapToLong(v -> v.getTotalRevenue() != null ? v.getTotalRevenue() : 0).sum();
                labels.add(String.valueOf(year));
                values.add(revenue);
            }
        }

        return new RevenueTrendResponse(labels, values);
    }
}
