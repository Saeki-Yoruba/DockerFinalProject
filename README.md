# 🧾 SupernovaPOS — 系統簡介

SupernovaPOS 是一套以 **Spring Boot 3.x + Vue 3 + MS SQL Server + Docker** 打造的餐飲 POS 與線上點餐系統，支援會員註冊登入、桌位管理、多人協作點餐、金流串接、通知推播、報表統計等功能。

本文件整理後端 **分工內容** 與 **package 架構說明**，方便面試、Demo 與上線維護。

---

# 👥 分工內容

## 帳號擁有者（組長） — 會員 / RBAC / OAuth / 報表

* 會員註冊登入（Email 驗證、重寄驗證信）
* Google / LINE OAuth2 第三方登入綁定
* RBAC 權限架構：User / Role / Permission / PermissionCategory
* JWT 登入/登出流程
* Dashboard 報表（每日營收、會員消費、熱門品項、集團訂單統計）
* Docker、後端環境設定、CI/CD 基礎

## 組員 A — POS 桌位 / 預約 / 即時通知

* 桌位狀態管理
* 預約功能
* WebSocket 即時推播
* Discord Webhook 整合

## 組員 B — 多人協作點餐 / 出餐管理 / 一次性 QR Code

* 各桌多人同時點餐 / 合單
* 出餐流程
* 一次性 QR Code 產生
* 餐點分類、產品管理 CRUD

## 組員 C — 綠界金流 / 點數機制 / 電子發票

* 綠界金流串接：建立訂單、callback、Return URL
* 點數機制
* 電子發票寄送功能
* Mail 寄送

---

# 🗂 Backend Package 說明

我的後端使用標準 **三層架構 + DTO + Mapper**，並搭配 RBAC 與 OAuth2。

```
com.supernova.pos
│
├── analytics                      # 報表（使用者負責）
│
├── auth                           # 登入、註冊
│
├── oauth                          # google/line OAuth2 callback、ImageBB 圖片託管
│
├── user                            # 會員資料、點數、綁定
│
├── store                           # 商店基本資料 CRUD
│
├── common                          # 整組共用package
│
└── SupernovaPosApplication.java
```

---

# 📌 各 Layer 分工邏輯

### ✔ Controller

* 接收前端請求
* 驗證基本參數
* 呼叫 Service
* 回傳 DTO
* **不得寫業務邏輯 / SQL**

### ✔ Service

* 商業邏輯
* 權限判斷
* 組合多 Repository 資料
* 呼叫外部 API（ECPay、Email、Webhook）

### ✔ Repository

* **只做資料存取**
* 不做運算、不組裝 DTO、不做業務判斷

### ✔ DTO / Mapper

* Request/Response 解耦
* MapStruct 自動轉換 entity ⇄ DTO

### ✔ Config

* Security、CORS、WebSocket、Swagger
* 第三方設定（OAuth2、ECPay）
