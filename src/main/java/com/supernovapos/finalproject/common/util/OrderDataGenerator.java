package com.supernovapos.finalproject.common.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.model.TempUser;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;
import com.supernovapos.finalproject.order.repository.TempUserRepository;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDataGenerator {
    private final OrdersRepository orderRepo;
    private final OrderGroupRepository groupRepo;
    private final TempUserRepository tempUserRepo;
    private final UserRepository userRepo;

    @Transactional
    public void generateOrders(int count) {
        List<OrderGroup> groups = groupRepo.findAll();
        List<TempUser> temps = tempUserRepo.findAll();
        List<User> users = userRepo.findAll();

        Random random = new Random();
        List<Orders> orders = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Orders o = new Orders();
            o.setTotalAmount(random.nextInt(1500) + 100);
            o.setStatus(true);
            o.setNote("測試訂單 " + (i+1));
            o.setCreatedAt(LocalDateTime.now().minusMinutes(i*3));
            o.setUpdatedAt(LocalDateTime.now());

            // 設定關聯 (JPA 會自動處理外鍵)
            o.setOrderGroup(groups.get(random.nextInt(groups.size())));
            o.setTempUser(temps.get(random.nextInt(temps.size())));
            o.setUser(users.get(random.nextInt(users.size())));

            orders.add(o);
        }
        orderRepo.saveAll(orders);
    }
}
