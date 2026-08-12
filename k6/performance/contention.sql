\set ON_ERROR_STOP on
\connect kyro_cart
UPDATE cart_items
SET product_id = 1, product_name = 'iPhone 15 Pro Max', price = 34990000,
    size = '256GB', discount_percent = 6, discounted_price = 32890600
WHERE cart_id BETWEEN 100000 AND 139999;

\connect kyro_catalog
UPDATE sizes SET quantity = 100000 WHERE id = 1;
