CREATE VIEW v_product_sales AS
SELECT 
    p.id              AS product_id,
    p.name            AS product_name,
    c.category_name   AS category,
    SUM(oi.quantity)  AS total_quantity,
    SUM(oi.subtotal)  AS total_revenue
FROM order_items oi
JOIN products p ON oi.product_id = p.id
JOIN product_category c ON p.category_id = c.id
GROUP BY p.id, p.name, c.category_name;
GO

CREATE VIEW v_daily_sales AS
SELECT 
    CONVERT(date, o.created_at) AS order_date,
    SUM(oi.subtotal) AS daily_revenue,
    COUNT(DISTINCT o.id) AS order_count,
    SUM(oi.subtotal) * 1.0 / NULLIF(COUNT(DISTINCT o.id), 0) AS avg_order_value
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
GROUP BY CONVERT(date, o.created_at);
GO

CREATE VIEW v_user_spending AS
SELECT 
    u.id AS user_id,
    u.nickname,
    u.email,
    SUM(oi.subtotal) AS total_spent,
    COUNT(DISTINCT o.id) AS order_count,
    MAX(o.created_at) AS last_order_date
FROM users u
JOIN orders o ON u.id = o.user_id
JOIN order_items oi ON o.id = oi.order_id
GROUP BY u.id, u.nickname, u.email;
GO

CREATE VIEW v_group_orders AS
SELECT 
    g.id AS group_id,
    g.table_id,
    COUNT(o.id) AS total_orders,
    SUM(o.total_amount) AS total_revenue,
    g.created_at,
    g.completed_at,
    DATEDIFF(MINUTE, g.created_at, g.completed_at) AS dining_duration
FROM order_groups g
LEFT JOIN orders o ON g.id = o.group_id
GROUP BY g.id, g.table_id, g.created_at, g.completed_at;