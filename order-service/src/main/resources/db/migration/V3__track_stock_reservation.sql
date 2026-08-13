ALTER TABLE orders
  ADD COLUMN stock_reserved boolean NOT NULL DEFAULT false;

UPDATE orders
SET stock_reserved = true
WHERE order_status IN ('CONFIRMED', 'SHIPPED', 'DELIVERED');
