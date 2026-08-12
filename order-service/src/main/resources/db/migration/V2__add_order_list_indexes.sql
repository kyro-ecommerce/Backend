CREATE INDEX idx_orders_user_date_id ON orders (user_id, order_date DESC, id DESC);
CREATE INDEX idx_orders_status_date_id ON orders (order_status, order_date DESC, id DESC);
