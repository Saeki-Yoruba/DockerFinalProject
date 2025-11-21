package com.supernovapos.finalproject.analytics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.analytics.model.dto.ProductImageResponse;
import com.supernovapos.finalproject.analytics.model.dto.ProductSaleRequest;
import com.supernovapos.finalproject.analytics.model.dto.ProductSaleResponse;
import com.supernovapos.finalproject.analytics.model.entity.ProductSalesView;
import com.supernovapos.finalproject.analytics.model.mapper.ProductSaleMapper;
import com.supernovapos.finalproject.analytics.repository.ProductSalesRepository;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSalesService {

	private final ProductSalesRepository productSalesRepository;
	private final ProductsRepository productRepo;
    private final ProductSaleMapper productSaleMapper;

    public List<ProductSaleResponse> getAllProductReport() {
        return productSalesRepository.findAll()
                .stream()
                .map(productSaleMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductImageResponse> getTop3PopularProducts() {
        // 撈出前 3 名
        List<ProductSalesView> top3 = productSalesRepository.findAll(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "totalQuantity"))
        ).getContent();

        return top3.stream()
                .map(sales -> {
                    String image = productRepo.findById(sales.getProductId())
                            .map(Products::getImage)
                            .orElse(null);
                    return new ProductImageResponse(
                            sales.getProductId(),
                            sales.getProductName(),
                            image
                    );
                })
                .toList();
    }
    
    public List<ProductSaleResponse> searchProductReport(ProductSaleRequest request) {
        // 1️ 動態條件
        Specification<ProductSalesView> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 判斷非 null 且非空字串才加條件
            if (request.category() != null && !request.category().isBlank()) {
                predicates.add(cb.equal(root.get("category"), request.category()));
            }
            if (request.minRevenue() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalRevenue"), request.minRevenue()));
            }
            if (request.maxRevenue() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalRevenue"), request.maxRevenue()));
            }
            if (request.minQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalQuantity"), request.minQuantity()));
            }
            if (request.maxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalQuantity"), request.maxQuantity()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2️ 排序 & Top N
        String sortBy = switch (request.sortBy()) {
            case "totalQuantity" -> "totalQuantity";
            case "totalRevenue" -> "totalRevenue";
            default -> "totalRevenue";
        };

        Sort.Direction direction =
            "ASC".equalsIgnoreCase(request.sortOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        int top;
        if (request.top() == null) {
            top = 10;
        } else if (request.top() == 0) {
            top = Integer.MAX_VALUE;
        } else {
            top = request.top();
        }

        PageRequest pageRequest = PageRequest.of(0, top, Sort.by(direction, sortBy));

        // 3️ 查詢資料庫
        Page<ProductSalesView> page = productSalesRepository.findAll(spec, pageRequest);
        List<ProductSalesView> results = page.getContent();

        // 4️ Entity → DTO
        return results.stream()
                .map(productSaleMapper::toDto)
                .toList();
    }

}
