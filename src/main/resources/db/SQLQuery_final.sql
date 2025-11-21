-- create database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'final_project')
BEGIN
    CREATE DATABASE final_project;
END
GO
USE final_project;
GO

CREATE TABLE users (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    phone_number    VARCHAR(20) NOT NULL UNIQUE,
    email_verified  BIT NOT NULL DEFAULT 0,
    google_uid      VARCHAR(255) NULL,
    line_uid        VARCHAR(255) NULL,
    nickname        NVARCHAR(50),
    avatar          VARCHAR(MAX),
    birthdate       DATE,
    invoice_carrier VARCHAR(20),
    point           INT NOT NULL DEFAULT 0,
    is_active       BIT NOT NULL DEFAULT 1,
    created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE roles (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    code       VARCHAR(50) NOT NULL UNIQUE,
    name       VARCHAR(50) NOT NULL,
    category   VARCHAR(50) NOT NULL DEFAULT 'STORE'
        CHECK (category IN ('ADMIN', 'STORE', 'USER')),
    is_active  BIT NOT NULL DEFAULT 1,  -- 1=啟用, 0=停用
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE user_roles (
    user_id    BIGINT NOT NULL,
    role_id    INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE
);
GO

CREATE TABLE permission_category (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(255),
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE permissions (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_id  INT NULL,
    code         VARCHAR(100) NOT NULL UNIQUE,
    http_method  VARCHAR(10) NOT NULL DEFAULT 'GET' CHECK (http_method IN ('GET','POST','PUT','DELETE','PATCH')),
    url          VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    is_avaliable BIT NOT NULL DEFAULT 1,
    created_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    FOREIGN KEY (category_id) REFERENCES permission_category(id) ON DELETE SET NULL ON UPDATE CASCADE
);
GO

CREATE TABLE role_permissions (
    role_id       INT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE ON UPDATE CASCADE
);
GO

CREATE TABLE verification_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY, 
    user_id BIGINT NOT NULL, 
    token NVARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME2 NOT NULL,

    CONSTRAINT fk_verification_user FOREIGN KEY (user_id)
        REFERENCES users(id) 
        ON DELETE CASCADE 
);
GO

CREATE TABLE stores (
    id                  INT IDENTITY(1,1) PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    address             NVARCHAR(200),
    description         NVARCHAR(200),
    logo_url            VARCHAR(200),
    banner_url          VARCHAR(200),
    layout_url          VARCHAR(200),
    welcome_message     NVARCHAR(200),
    points_per_currency FLOAT NOT NULL DEFAULT 100.0,
    currency_per_point  FLOAT NOT NULL DEFAULT 1.0,
    is_active           BIT NOT NULL DEFAULT 1,
    created_at          DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE restaurant_table (
    id INT IDENTITY(1,1) PRIMARY KEY,
    table_id INT NOT NULL UNIQUE,  
    capacity INT NOT NULL CHECK (capacity > 0),         
    shape VARCHAR(20),             
    pos_x INT,                     
    pos_y INT,                    
    is_available VARCHAR(20) CHECK (is_available IN ('dining', 'cleaning', 'booked', 'empty')) DEFAULT 'empty'  
);
GO

CREATE TABLE business_hours (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    day_of_week TINYINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),  
    open_time TIME NOT NULL,       
    close_time TIME NOT NULL,      
    is_active BIT DEFAULT 1,       
    created_at DATETIME2 DEFAULT GETDATE(),  
    updated_at DATETIME2 DEFAULT GETDATE(),  
    CONSTRAINT chk_open_before_close CHECK (open_time < close_time)  
);
GO

CREATE TABLE store_holidays (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    holiday_date DATE NOT NULL,                    
    is_recurring BIT DEFAULT 0,                   
    reason NVARCHAR(200),                          
    created_at DATETIME2 DEFAULT GETDATE(),        
    updated_at DATETIME2 DEFAULT GETDATE(),         
);
GO

CREATE TABLE reservations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,             
    email VARCHAR(100),                            
    people INT NOT NULL CHECK (people > 0),                          
    booked_name VARCHAR(50) NOT NULL,              
    note NVARCHAR(500),                            
    status VARCHAR(20) CHECK (status IN ('confirmed', 'cancelled')) DEFAULT 'confirmed',  
    created_at DATETIME2 DEFAULT GETDATE(),        
    time_choice VARCHAR(50) CHECK (time_choice IN (
        '11:00-12:30', '11:30-13:00', '12:00-13:30', '12:30-14:00', '13:00-14:30', '13:30-15:00',
        '17:00-18:30', '17:30-19:00', '18:00-19:30', '18:30-20:00', '19:00-20:30', '19:30-21:00'
    )),                        
    checkin_status BIT DEFAULT 0,                 
    reservation_date DATE NOT NULL,                
    table_id INT,                                  
    FOREIGN KEY (table_id) REFERENCES restaurant_table(table_id)
);
GO

CREATE TABLE product_category (
    id            INT IDENTITY(1,1) PRIMARY KEY,      
    category_name VARCHAR(50)    NOT NULL UNIQUE,
    is_active     BIT            NOT NULL DEFAULT (1),
    created_at    DATETIME2(0)   NOT NULL DEFAULT (SYSDATETIME())
);
GO

CREATE TABLE products (
    id            INT IDENTITY(1,1) PRIMARY KEY, 
    name          VARCHAR(100)   NOT NULL, 
    category_id   INT            NULL, 
    price         DECIMAL(10,2)  NOT NULL,
    is_available  BIT            NOT NULL DEFAULT (1),
    image         VARCHAR(MAX)   NULL,
    description   NVARCHAR(255)   NULL,
    created_at    DATETIME2(0)   NOT NULL DEFAULT (SYSDATETIME()),
    updated_at    DATETIME2(0)   NOT NULL DEFAULT (SYSDATETIME()),

    CONSTRAINT FK_products_category FOREIGN KEY (category_id) 
        REFERENCES product_category(id) 
        ON DELETE SET NULL ON UPDATE CASCADE
);
GO

CREATE TABLE order_groups (
    id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    table_id INT NOT NULL,
    total_amount INT NOT NULL,
    status BIT NOT NULL DEFAULT 1,
    has_Order BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    completed_at DATETIME2 NULL,
    CONSTRAINT PK_order_groups PRIMARY KEY (id),
    CONSTRAINT FK_order_groups_table
        FOREIGN KEY (table_id) REFERENCES dbo.restaurant_table(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

CREATE TABLE temp_user (
    id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    nickname NVARCHAR(255) NOT NULL,
    is_register BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    order_group_id UNIQUEIDENTIFIER,
    
    -- 主鍵約束
    CONSTRAINT PK_temp_user PRIMARY KEY (id),

    -- 外鍵約束
    CONSTRAINT FK_temp_user_order_group FOREIGN KEY (order_group_id) 
        REFERENCES order_groups(id)
);
GO

CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) NOT NULL,
    total_amount INT NOT NULL DEFAULT 0,
    status BIT NOT NULL DEFAULT 0,
    note NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    group_id UNIQUEIDENTIFIER,
    temp_user_id UNIQUEIDENTIFIER,
    user_id BIGINT,
    
    -- 主鍵約束
    CONSTRAINT PK_orders PRIMARY KEY (id),
    
    -- 外鍵約束
    CONSTRAINT FK_orders_group FOREIGN KEY (group_id) REFERENCES order_groups(id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_temp_user FOREIGN KEY (temp_user_id) REFERENCES temp_user(id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) NOT NULL,
    order_id BIGINT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal AS (CAST(quantity AS DECIMAL(10,2)) * unit_price) PERSISTED,
    note NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_order_items PRIMARY KEY (id),
    CONSTRAINT FK_order_items_order
        FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT FK_order_items_product
        FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_group_id UNIQUEIDENTIFIER NOT NULL,
    payer_user_id BIGINT NULL,
    merchant_trade_no NVARCHAR(20) NOT NULL,
    trade_no NVARCHAR(20) NULL,
    trade_desc NVARCHAR(200) NOT NULL,
    payment_type NVARCHAR(20) NOT NULL DEFAULT 'aio',
    choose_payment NVARCHAR(20) NOT NULL DEFAULT 'Credit',
    trade_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (trade_status IN ('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED', 'CANCELLED')), -- ✅ 直接合併回來
    total_amount INT NOT NULL CHECK (total_amount >= 0),
    points_used INT NOT NULL DEFAULT 0 CHECK (points_used >= 0),
    points_discount INT NOT NULL DEFAULT 0 CHECK (points_discount >= 0),
    rtn_code NVARCHAR(10) NULL,
    rtn_msg NVARCHAR(200) NULL,
    trade_date NVARCHAR(20) NULL,
    paid_at DATETIME NULL,
    simulate_paid BIT NOT NULL DEFAULT 0,
    check_mac_value NVARCHAR(MAX) NULL,
    payment_result NVARCHAR(MAX) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),

    -- 約束
    CONSTRAINT UQ_payments_merchant_trade_no UNIQUE (merchant_trade_no),

    -- 外來鍵
    CONSTRAINT FK_payments_order_groups FOREIGN KEY (order_group_id) REFERENCES order_groups(id),
    CONSTRAINT FK_payments_payer FOREIGN KEY (payer_user_id) REFERENCES users(id)
);
GO

CREATE TABLE points (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type NVARCHAR(20) NOT NULL
        CHECK (type IN ('order_earn', 'order_use', 'admin_grant', 'admin_deduct', 'expired', 'refund')), -- ✅ 合併回來
    points_amount INT NOT NULL,
    balance_after INT NOT NULL CHECK (balance_after >= 0),
    order_group_id UNIQUEIDENTIFIER NULL,
    payment_id BIGINT NULL,
    expired_at DATETIME NULL,
    is_expired BIT NOT NULL DEFAULT 0,
    description NVARCHAR(200) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_points_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_points_order_groups FOREIGN KEY (order_group_id) REFERENCES order_groups(id),
    CONSTRAINT FK_points_payments FOREIGN KEY (payment_id) REFERENCES payments(id)
);
GO

CREATE TABLE invoices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    payer_user_id BIGINT NOT NULL,
    invoice_number NVARCHAR(20) NOT NULL,
    invoice_status NVARCHAR(20) NOT NULL DEFAULT 'issued'
        CHECK (invoice_status IN ('issued', 'cancelled', 'returned')),
    amount INT NOT NULL CHECK (amount >= 0),
    tax_amount INT NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    total_amount INT NOT NULL CHECK (total_amount >= 0),
    carrier NVARCHAR(20) NULL,
    issued_at DATETIME NOT NULL DEFAULT GETDATE(),

    -- 約束
    CONSTRAINT UQ_invoices_invoice_number UNIQUE (invoice_number),

    -- 外來鍵
    CONSTRAINT FK_invoices_payments FOREIGN KEY (payment_id) REFERENCES payments(id),
    CONSTRAINT FK_invoices_payer FOREIGN KEY (payer_user_id) REFERENCES users(id)
);
GO

CREATE UNIQUE INDEX UQ_users_google_uid
ON dbo.users(google_uid)
WHERE google_uid IS NOT NULL;
GO

CREATE UNIQUE INDEX UQ_users_line_uid
ON dbo.users(line_uid)
WHERE line_uid IS NOT NULL;
GO

CREATE TRIGGER trg_order_groups_set_updated_at
ON dbo.order_groups
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE og
    SET updated_at = SYSUTCDATETIME()
    FROM dbo.order_groups og
    INNER JOIN inserted i ON og.id = i.id;
END;
GO

CREATE TRIGGER trg_orders_set_updated_at
ON dbo.orders
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE o
    SET updated_at = SYSUTCDATETIME()
    FROM dbo.orders o
    INNER JOIN inserted i ON o.id = i.id;
END;