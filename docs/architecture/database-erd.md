# Database-per-service ERD

Use [database.dbml](database.dbml) in dbdiagram.io or
[database-erd.puml](database-erd.puml) in draw.io. Both diagrams show only physical foreign keys
inside a service database.

```mermaid
flowchart LR
  Auth[Auth Service] --> AuthDB[(kyro_auth)]
  Catalog[Catalog Service] --> CatalogDB[(kyro_catalog)]
  Order[Order Service] --> OrderDB[(kyro_order)]
  Payment[Payment Service] --> PaymentDB[(kyro_payment)]
  Cart[Cart Service] --> Redis[(Redis: cart:{userId})]

  Order -. Feign .-> Auth
  Order -. Feign .-> Catalog
  Order -. Feign .-> Cart
  Payment --> RabbitMQ[RabbitMQ]
  RabbitMQ --> Order
```

- `review.user_id`, `orders.user_id`, `order_item.product_id`, and
  `payment_details.order_id` are external references, not SQL foreign keys.
- Cloudinary stores image files; `kyro_catalog.image` stores their URL and metadata.
- `orders` preserves product and shipping snapshots for order history.
