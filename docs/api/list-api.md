# Product and order list APIs

All list endpoints use zero-based pagination. `page` defaults to `0`, `size` defaults to `20`,
and `size` must be between `1` and `100`. Repeat `sort=field,direction` to sort by multiple
fields. Responses contain `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and
`last`. Invalid filters and sort fields return an RFC 7807 response with code `INVALID_ARGUMENT`.

## Products

`GET /api/v1/products` and `GET /api/v1/admin/products` accept `keyword`, `categoryId`, `brand`,
`color`, `minPrice`, `maxPrice`, `inStock`, and `minRating`. A level-one `categoryId` includes its
children. Price filters use the discounted price.

Product sort fields are `id`, `title`, `brand`, `price`, `discountPercent`, `createdAt`,
`averageRating`, and `quantitySold`. The admin endpoint also accepts `quantity`.

Example:

```text
GET /api/v1/products?categoryId=11&minPrice=10000000&inStock=true&page=0&size=20&sort=price,asc&sort=createdAt,desc
```

## Orders

`GET /api/v1/orders` accepts `status`, `paymentMethod`, `paymentStatus`, `startDate`, `endDate`,
`minTotal`, and `maxTotal`; it always limits results to the authenticated user.

`GET /api/v1/admin/orders` accepts the same filters plus `search` and `userId`. Search covers order
ID, customer email, recipient name, and phone number. Dates use `yyyy-MM-dd` and are inclusive.

Order sort fields are `id`, `orderDate`, `deliveryDate`, `totalDiscountedPrice`, and `totalItems`.

Example:

```text
GET /api/v1/orders?status=DELIVERED&startDate=2026-01-01&page=0&size=20&sort=orderDate,desc
```
