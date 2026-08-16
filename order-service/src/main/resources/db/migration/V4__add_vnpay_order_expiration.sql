ALTER TABLE orders ADD COLUMN expires_at timestamp with time zone;

UPDATE orders
SET expires_at = order_date + INTERVAL '15 minutes'
WHERE order_status = 'PENDING'
  AND payment_method = 'VNPAY'
  AND payment_status IS DISTINCT FROM 'COMPLETED';

CREATE INDEX idx_orders_expired_vnpay
ON orders (expires_at, id)
WHERE order_status = 'PENDING'
  AND payment_method = 'VNPAY'
  AND payment_status IS DISTINCT FROM 'COMPLETED';
