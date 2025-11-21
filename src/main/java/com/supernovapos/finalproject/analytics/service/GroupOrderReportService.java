package com.supernovapos.finalproject.analytics.service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.analytics.model.dto.GroupOrderReportDto;
import com.supernovapos.finalproject.analytics.model.dto.KpiDto;
import com.supernovapos.finalproject.analytics.model.dto.TableRankingDto;
import com.supernovapos.finalproject.analytics.model.entity.GroupOrdersView;
import com.supernovapos.finalproject.analytics.repository.GroupOrdersRepository;
import com.supernovapos.finalproject.analytics.repository.GroupOrdersSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupOrderReportService {

    private final GroupOrdersRepository repository;

    public GroupOrderReportDto getReport(LocalDate date, YearMonth ym, Integer year) {
        Specification<GroupOrdersView> spec;

        if (date != null) {
            spec = GroupOrdersSpecifications.onDate(date);
        } else if (ym != null) {
            spec = GroupOrdersSpecifications.inMonth(ym);
        } else if (year != null) {
            spec = GroupOrdersSpecifications.inYear(year);
        } else {
            spec = GroupOrdersSpecifications.onDate(LocalDate.now().minusDays(1)); // 預設昨天
        }

        List<GroupOrdersView> result = repository.findAll(spec);

        KpiDto kpi = calculateKpi(result);
        Map<String, Long> periodDist = calculatePeriodDistribution(result);
        List<TableRankingDto> topTables = calculateTopTables(result);

        return new GroupOrderReportDto(kpi, periodDist, topTables);
    }

    // -------- Private Methods --------

    private KpiDto calculateKpi(List<GroupOrdersView> result) {
        long groupCount = result.size();
        long totalRevenue = result.stream()
                                  .mapToLong(v -> v.getTotalRevenue() == null ? 0 : v.getTotalRevenue())
                                  .sum();
        double avgRevenue = groupCount > 0 ? (double) totalRevenue / groupCount : 0.0;
        double avgDuration = calculateAvgDiningDuration(result);

        return new KpiDto(groupCount, totalRevenue, avgRevenue, avgDuration);
    }

    private double calculateAvgDiningDuration(List<GroupOrdersView> result) {
        return result.stream()
                     .filter(v -> v.getDiningDuration() != null)
                     .mapToInt(GroupOrdersView::getDiningDuration)
                     .average()
                     .orElse(0.0);
    }

    private Map<String, Long> calculatePeriodDistribution(List<GroupOrdersView> result) {
        return result.stream()
            .collect(Collectors.groupingBy(
                v -> {
                    int hour = v.getCreatedAt().getHour();
                    if (hour >= 11 && hour <= 14) return "午餐";
                    else if (hour >= 17 && hour <= 21) return "晚餐";
                    else return "其他";
                },
                Collectors.counting()
            ));
    }

    private List<TableRankingDto> calculateTopTables(List<GroupOrdersView> result) {
        return result.stream()
            .collect(Collectors.groupingBy(
                GroupOrdersView::getTableId,
                Collectors.summingLong(v -> v.getTotalRevenue() == null ? 0 : v.getTotalRevenue())
            ))
            .entrySet().stream()
            .map(e -> new TableRankingDto(e.getKey(), e.getValue()))
            .sorted((a, b) -> Long.compare(b.getRevenue(), a.getRevenue())) // 營收由大到小
            .limit(5)
            .toList();
    }
}
