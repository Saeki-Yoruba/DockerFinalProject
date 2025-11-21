package com.supernovapos.finalproject.store.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.store.model.Store;
import com.supernovapos.finalproject.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class StoreInitializer implements CommandLineRunner {

    private final StoreRepository storeRepository;

    @Override
    public void run(String... args) {
        if (storeRepository.count() == 0) {
            Store store = new Store();
            store.setName("丸竹儀");
            store.setPhone("02-123-1234");
            store.setAddress("106 台北市大安區復興南路一段390號2樓");
            store.setWelcomeMessage("歡迎光臨丸竹儀，祝您用餐愉快，度過美好時光。");
            store.setDescription("提供美味創意料理，結合簡單便利的智慧點餐服務，讓您安心享受美味餐點與悠閒時光。");
            store.setLogoUrl("/images/logo.png");
            store.setBannerUrl("/images/banner-menu.png");
            store.setLayoutUrl("/images/layout.png");
            store.setIsActive(true);

            storeRepository.save(store);
            log.info("預設商店已建立: {}", store.getName());
        } else {
            log.info("已存在商店資料，略過初始化");
        }
    }
}