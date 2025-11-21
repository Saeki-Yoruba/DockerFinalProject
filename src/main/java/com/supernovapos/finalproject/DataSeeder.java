package com.supernovapos.finalproject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.cart.repo.OrderRepository;
import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.model.TempUser;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrderItemsRepository;
import com.supernovapos.finalproject.order.repository.TempUserRepository;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductsRepository;
import com.supernovapos.finalproject.table.model.RestaurantTable;
import com.supernovapos.finalproject.table.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final OrderGroupRepository orderGroupRepository;
    private final TempUserRepository tempUserRepository;
    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final ProductsRepository productRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        List<RestaurantTable> tables = tableRepository.findAll();
        List<Products> products = productRepository.findAll();

        if (tables.isEmpty() || products.isEmpty()) {
            System.out.println("⚠️ 尚未有桌子或商品資料，請先建立餐桌與商品！");
            return;
        }

        int groupCount = 200; // 生成 200 筆群組訂單
        for (int i = 0; i < groupCount; i++) {
            generateOrderGroup(tables, products);
        }
        System.out.println("✅ 假資料建立完成: " + groupCount + " 筆群組訂單");
    }

    private void generateOrderGroup(List<RestaurantTable> tables, List<Products> products) {
        // 隨機桌子
        RestaurantTable table = tables.get(random.nextInt(tables.size()));

        // 建立群組訂單
        OrderGroup group = new OrderGroup();
        group.setTable(table);
        group.setStatus(false); // 已完成
        group.setHasOrder(true);
        group.setTotalAmount(0); // ✅ 避免 NULL 問題

        // 隨機營業時間內的開始時間
        LocalDateTime start = randomBusinessTime();
        LocalDateTime end = start.plusMinutes(60 + random.nextInt(61)); // ✅ 用餐 60–120 分鐘
        group.setCreatedAt(start);
        group.setCompletedAt(end);

        group = orderGroupRepository.save(group);

        // 隨機 2–6 人
        int personCount = 2 + random.nextInt(5);
        int groupTotal = 0;

        for (int i = 0; i < personCount; i++) {
            // 建立臨時用戶
            TempUser tempUser = new TempUser();
            tempUser.setNickname("訪客" + random.nextInt(1000));
            tempUser.setIsRegister(false);
            tempUser.setOrderGroup(group);
            tempUser = tempUserRepository.save(tempUser);

            // 個人訂單
            Orders order = new Orders();
            order.setOrderGroup(group);
            order.setTempUser(tempUser);
            order.setStatus(true); // 已完成
            order.setCreatedAt(start.plusMinutes(random.nextInt(20)));
            order.setTotalAmount(0); // ✅ 預設 0

            order = orderRepository.save(order);

            int orderTotal = 0;
            // 每人 1–3 個商品
            int itemCount = 1 + random.nextInt(3);
            for (int j = 0; j < itemCount; j++) {
                Products product = products.get(random.nextInt(products.size()));
                int qty = 1 + random.nextInt(2);

                OrderItems item = new OrderItems();
                item.setOrders(order);
                item.setProducts(product);
                item.setQuantity(qty);
                item.setUnitPrice(product.getPrice());
                orderItemRepository.save(item);

                orderTotal += product.getPrice().intValue() * qty;
            }

            // 更新個人訂單金額
            order.setTotalAmount(orderTotal);
            orderRepository.save(order);

            groupTotal += orderTotal;
        }

        // 更新群組總金額
        group.setTotalAmount(groupTotal);
        orderGroupRepository.save(group);
    }

    private LocalDateTime randomBusinessTime() {
        // 生成 2025-06-01 ~ 2025-09-30 的隨機日期
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 30);

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate randomDate = startDate.plusDays(random.nextInt((int) days + 1));

        // 午餐時段 11:00–13:30 或 晚餐時段 17:30–20:00
        boolean lunch = random.nextBoolean();
        int hour = lunch ? 11 + random.nextInt(3) : 17 + random.nextInt(3);
        int minute = random.nextInt(2) * 30; // 整點或半點

        return randomDate.atTime(hour, minute);
    }
}