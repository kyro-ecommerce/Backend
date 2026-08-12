package com.kyro.catalog;

import com.kyro.catalog.client.OrderClient;
import com.kyro.catalog.dto.CreateProductRequest;
import com.kyro.catalog.dto.ProductDTO;
import com.kyro.catalog.dto.UpdateProductRequest;
import com.kyro.catalog.messaging.ProductEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ReviewRepository reviewRepository;
  private final ImageService imageService;
  private final ProductEventPublisher productEventPublisher;
  private final OrderClient orderClient;

  public ProductService(
      ProductRepository productRepository,
      CategoryRepository categoryRepository,
      ReviewRepository reviewRepository,
      ImageService imageService,
      ProductEventPublisher productEventPublisher,
      OrderClient orderClient) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
    this.reviewRepository = reviewRepository;
    this.imageService = imageService;
    this.productEventPublisher = productEventPublisher;
    this.orderClient = orderClient;
  }

  @Transactional(readOnly = true)
  public List<Product> findProductsByIds(List<Long> productIds) {
    return productRepository.findAllById(productIds);
  }

  @Transactional
  public Product createProduct(CreateProductRequest req) {
    // Logic for handling categories
    Category parentCategory = null;
    Category category = null;

    // Handle top level category
    if (req.getTopLevelCategory() != null && !req.getTopLevelCategory().isEmpty()) {
      parentCategory =
          categoryRepository
              .findByNameIgnoreCase(req.getTopLevelCategory())
              .orElseThrow(() -> new EntityNotFoundException("Top level category not found"));
      if (parentCategory.getLevel() != 1) {
        throw new IllegalArgumentException("Top level category must have level 1");
      }

      // Handle second level category if provided
      if (req.getSecondLevelCategory() != null && !req.getSecondLevelCategory().isEmpty()) {
        category =
            categoryRepository
                .findByNameIgnoreCase(req.getSecondLevelCategory())
                .orElseThrow(() -> new EntityNotFoundException("Second level category not found"));
        if (category.getLevel() != 2
            || category.getParentCategory() == null
            || !category.getParentCategory().getId().equals(parentCategory.getId())) {
          throw new IllegalArgumentException(
              "Second level category does not belong to top level category");
        }
      } else {
        // If no second level, use top level
        category = parentCategory;
      }
    }

    // Create the product
    Product product = new Product();
    product.setTitle(req.getTitle());
    product.setDescription(req.getDescription());
    product.setPrice(req.getPrice());
    product.setDiscountPersent(req.getDiscountPersent());
    product.setBrand(req.getBrand());
    product.setColor(req.getColor());
    product.setWeight(req.getWeight());
    product.setDimension(req.getDimension());
    product.setBatteryType(req.getBatteryType());
    product.setBatteryCapacity(req.getBatteryCapacity());
    product.setRamCapacity(req.getRamCapacity());
    product.setRomCapacity(req.getRomCapacity());
    product.setScreenSize(req.getScreenSize());
    product.setDetailedReview(req.getDetailedReview());
    product.setPowerfulPerformance(req.getPowerfulPerformance());
    product.setConnectionPort(req.getConnectionPort());
    product.setCreatedAt(LocalDateTime.now());
    product.setQuantity(req.getQuantity());
    product.setCategory(category);

    // Update discounted price
    product.updateDiscountedPrice();

    // Handle sizes if provided
    if (req.getSizes() != null) {
      for (ProductSize size : req.getSizes()) {
        size.setProduct(product);
      }
      product.setSizes(req.getSizes());
    }

    Product savedProduct = productRepository.save(product);
    // Publish event so AI Service can index this new product
    productEventPublisher.publishProductCreated(savedProduct);
    return savedProduct;
  }

  @Transactional
  public String deleteProduct(Long id) {
    Product product = findProductById(id);
    if (product == null) {
      throw new EntityNotFoundException("Product not found with id: " + id);
    }
    productRepository.delete(product);
    return "Product deleted successfully";
  }

  public Product findProductById(Long id) {
    Optional<Product> product = productRepository.findById(id);
    if (product.isPresent()) {
      return product.get();
    }
    throw new EntityNotFoundException("Product not found with id: " + id);
  }

  public List<Product> searchProducts(String keyword) {
    return productRepository.findByTitleContainingIgnoreCase(keyword);
  }

  public List<Product> findProductByCategory(String categoryName) {
    Category category = categoryRepository.findByName(categoryName);
    if (category == null) {
      return new ArrayList<>();
    }

    List<Long> categoryIdsToSearch = new ArrayList<>();
    if (category.getLevel() == 1) {
      categoryIdsToSearch.add(category.getId());
      List<Category> subCategories = categoryRepository.findByParentCategoryId(category.getId());
      subCategories.forEach(sub -> categoryIdsToSearch.add(sub.getId()));
    } else if (category.getLevel() == 2) {
      categoryIdsToSearch.add(category.getId());
    }

    if (categoryIdsToSearch.isEmpty()) {
      return new ArrayList<>();
    }

    return productRepository.findByCategoryIdIn(categoryIdsToSearch);
  }

  @Transactional(readOnly = true)
  public Page<ProductDTO> getProductsWithFilter(Pageable pageable, FilterProduct filter) {
    validateProductFilter(filter);

    List<Long> categoryIds = null;
    if (filter.getCategoryId() != null) {
      Optional<Category> category = categoryRepository.findById(filter.getCategoryId());
      if (category.isEmpty()) {
        return Page.empty(pageable);
      }
      categoryIds = new ArrayList<>();
      categoryIds.add(category.get().getId());
      if (category.get().getLevel() == 1) {
        categoryRepository.findByParentCategoryId(category.get().getId()).stream()
            .map(Category::getId)
            .forEach(categoryIds::add);
      }
    }

    String keyword = clean(filter.getKeyword());
    String brand = clean(filter.getBrand());
    String color = clean(filter.getColor());
    List<Long> selectedCategoryIds = categoryIds;
    Specification<Product> specification =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (keyword != null) {
            String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("brand")), pattern)));
          }
          if (selectedCategoryIds != null) {
            predicates.add(root.get("category").get("id").in(selectedCategoryIds));
          }
          if (brand != null) {
            predicates.add(cb.equal(cb.lower(root.get("brand")), brand.toLowerCase(Locale.ROOT)));
          }
          if (color != null) {
            predicates.add(cb.equal(cb.lower(root.get("color")), color.toLowerCase(Locale.ROOT)));
          }
          if (filter.getMinPrice() != null) {
            predicates.add(cb.ge(root.get("discountedPrice"), filter.getMinPrice()));
          }
          if (filter.getMaxPrice() != null) {
            predicates.add(cb.le(root.get("discountedPrice"), filter.getMaxPrice()));
          }
          if (filter.getInStock() != null) {
            predicates.add(
                filter.getInStock()
                    ? cb.gt(root.get("quantity"), 0)
                    : cb.le(root.get("quantity"), 0));
          }
          if (filter.getMinRating() != null) {
            predicates.add(cb.ge(root.get("averageRating"), filter.getMinRating()));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    return productRepository.findAll(specification, pageable).map(ProductDTO::new);
  }

  static Pageable productPageable(int page, int size, List<String> sortValues, boolean admin) {
    if (page < 0 || size < 1 || size > 100) {
      throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 100");
    }

    Map<String, String> fields =
        new HashMap<>(
            Map.of(
                "id", "id",
                "title", "title",
                "brand", "brand",
                "price", "discountedPrice",
                "discountPercent", "discountPersent",
                "createdAt", "createdAt",
                "averageRating", "averageRating",
                "quantitySold", "quantitySold"));
    if (admin) {
      fields.put("quantity", "quantity");
    }

    List<String> sortTokens = sortTokens(sortValues);
    List<Sort.Order> orders = new ArrayList<>();
    for (int index = 0; index < sortTokens.size(); index += 2) {
      String requestedProperty = sortTokens.get(index);
      String property = fields.get(requestedProperty);
      if (property == null) {
        throw new IllegalArgumentException("Unsupported product sort: " + requestedProperty);
      }
      orders.add(new Sort.Order(Sort.Direction.fromString(sortTokens.get(index + 1)), property));
    }
    if (orders.isEmpty()) {
      orders.add(Sort.Order.desc("createdAt"));
    }
    if (orders.stream().noneMatch(order -> order.getProperty().equals("id"))) {
      orders.add(new Sort.Order(orders.get(0).getDirection(), "id"));
    }
    return PageRequest.of(page, size, Sort.by(orders));
  }

  private static List<String> sortTokens(List<String> sortValues) {
    if (sortValues == null) {
      return List.of();
    }
    List<String> tokens =
        sortValues.stream()
            .flatMap(value -> Arrays.stream(value.split(",")))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .toList();
    if (tokens.size() % 2 != 0) {
      throw new IllegalArgumentException("sort must use field,direction pairs");
    }
    return tokens;
  }

  private static void validateProductFilter(FilterProduct filter) {
    if (filter.getMinPrice() != null && filter.getMinPrice() < 0
        || filter.getMaxPrice() != null && filter.getMaxPrice() < 0
        || filter.getMinPrice() != null
            && filter.getMaxPrice() != null
            && filter.getMinPrice() > filter.getMaxPrice()) {
      throw new IllegalArgumentException("Invalid product price range");
    }
    if (filter.getMinRating() != null && (filter.getMinRating() < 0 || filter.getMinRating() > 5)) {
      throw new IllegalArgumentException("minRating must be between 0 and 5");
    }
    if (clean(filter.getKeyword()) != null && clean(filter.getKeyword()).length() > 100) {
      throw new IllegalArgumentException("keyword must not exceed 100 characters");
    }
  }

  private static String clean(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  public Map<String, Object> getAdminFilterStatistics() {
    List<Product> allProducts = productRepository.findAll();
    Map<String, Object> stats = new HashMap<>();

    OptionalInt minPrice =
        allProducts.stream()
            .filter(p -> p.getDiscountedPrice() > 0)
            .mapToInt(Product::getDiscountedPrice)
            .min();
    OptionalInt maxPrice = allProducts.stream().mapToInt(Product::getDiscountedPrice).max();

    stats.put("priceRange", Map.of("min", minPrice.orElse(0), "max", maxPrice.orElse(0)));

    long inStockCount = allProducts.stream().filter(p -> p.getQuantity() > 0).count();
    long outOfStockCount = allProducts.size() - inStockCount;
    long totalSoldItems =
        allProducts.stream()
            .mapToLong(p -> p.getQuantitySold() != null ? p.getQuantitySold() : 0L)
            .sum();

    stats.put("totalProducts", allProducts.size());
    stats.put("inStock", inStockCount);
    stats.put("soldItems", totalSoldItems);

    stats.put(
        "stockStatus",
        Map.of(
            "inStock", inStockCount, "outOfStock", outOfStockCount, "total", allProducts.size()));

    List<String> colors =
        allProducts.stream()
            .map(Product::getColor)
            .filter(color -> color != null && !color.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    stats.put("colors", colors);
    stats.put("categories", getAllCategories());

    return stats;
  }

  public Map<String, Object> getAllCategories() {
    Map<String, Object> categoriesMap = new HashMap<>();
    List<String> topLevelCategories = productRepository.findDistinctTopLevelCategories();
    categoriesMap.put("topLevel", topLevelCategories);

    Map<String, List<String>> secondLevelByTopLevel = new HashMap<>();
    for (String topLevel : topLevelCategories) {
      List<String> secondLevel =
          productRepository.findDistinctSecondLevelCategoriesByTopLevel(topLevel);
      if (!secondLevel.isEmpty()) {
        secondLevelByTopLevel.put(topLevel, secondLevel);
      }
    }
    categoriesMap.put("secondLevel", secondLevelByTopLevel);
    return categoriesMap;
  }

  @Transactional
  public void adminDeleteProduct(Long productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

    List<Review> reviews = reviewRepository.findAllByProductId(productId);
    if (!reviews.isEmpty()) {
      reviewRepository.deleteAll(reviews);
    }

    imageService.deleteAllProductImages(productId);
    productRepository.delete(product);
    // Publish DELETED event so AI Service deactivates this product in its index
    productEventPublisher.publishProductDeleted(productId);
  }

  public List<Map<String, Object>> getTopSellingProducts(int limit) {
    if (limit < 1) {
      throw new IllegalArgumentException("Top-selling limit must be positive");
    }
    List<OrderClient.TopSellingProductResponse> sales = orderClient.getTopSellingProducts(limit);
    List<Product> products =
        productRepository
            .findAllById(
                sales.stream().map(OrderClient.TopSellingProductResponse::productId).toList())
            .stream()
            .toList();
    return mapTopSellingProducts(sales, products);
  }

  static List<Map<String, Object>> mapTopSellingProducts(
      List<OrderClient.TopSellingProductResponse> sales, List<Product> products) {
    Map<Long, Product> productsById =
        products.stream().collect(Collectors.toMap(Product::getId, product -> product));
    List<Map<String, Object>> result = new ArrayList<>();

    for (OrderClient.TopSellingProductResponse sale : sales) {
      Product product = productsById.get(sale.productId());
      if (product != null) {
        Map<String, Object> productMap = mapProductToMap(product);
        productMap.put("quantity_sold", sale.quantitySold());
        productMap.put("quantitySold", sale.quantitySold());
        result.add(productMap);
      }
    }
    return result;
  }

  public Map<String, Object> getRevenueByCateogry() {
    Map<String, Object> result = new HashMap<>();
    List<Product> allProducts = productRepository.findAll();
    Map<String, Double> categoryRevenue = new HashMap<>();

    for (Product product : allProducts) {
      String categoryName;
      if (product.getCategory() != null) {
        if (product.getCategory().getLevel() == 2
            && product.getCategory().getParentCategory() != null) {
          categoryName = product.getCategory().getParentCategory().getName();
        } else {
          categoryName = product.getCategory().getName();
        }
      } else {
        categoryName = "Uncategorized";
      }

      Double revenue = categoryRevenue.getOrDefault(categoryName, 0.0);
      long quantitySoldValue = (product.getQuantitySold() != null) ? product.getQuantitySold() : 0L;
      revenue += (double) product.getDiscountedPrice() * quantitySoldValue;
      categoryRevenue.put(categoryName, revenue);
    }

    result.put("categoryRevenue", categoryRevenue);
    return result;
  }

  @Transactional
  public Product updateProduct(Long id, Product product) {
    Product existingProduct = findProductById(id);

    if (product.getTitle() != null) existingProduct.setTitle(product.getTitle());
    if (product.getDescription() != null) existingProduct.setDescription(product.getDescription());
    if (product.getBrand() != null) existingProduct.setBrand(product.getBrand());
    if (product.getColor() != null) existingProduct.setColor(product.getColor());

    if (product.getPrice() > 0) existingProduct.setPrice(product.getPrice());
    if (product.getDiscountPersent() >= 0)
      existingProduct.setDiscountPersent(product.getDiscountPersent());
    existingProduct.updateDiscountedPrice();

    if (product.getQuantity() >= 0) existingProduct.setQuantity(product.getQuantity());

    if (product.getCategory() != null && product.getCategory().getId() != null) {
      Category category =
          categoryRepository
              .findById(product.getCategory().getId())
              .orElseThrow(() -> new EntityNotFoundException("Category not found"));
      existingProduct.setCategory(category);
    }

    return productRepository.save(existingProduct);
  }

  @Transactional
  public ProductDTO updateProductByID(Long productId, UpdateProductRequest product) {
    Product curProduct = findProductById(productId);

    if (product.getTitle() != null) curProduct.setTitle(product.getTitle());
    if (product.getDescription() != null) curProduct.setDescription(product.getDescription());
    if (product.getBrand() != null) curProduct.setBrand(product.getBrand());
    if (product.getColor() != null) curProduct.setColor(product.getColor());

    if (product.getPrice() != null && product.getPrice() > 0)
      curProduct.setPrice(product.getPrice());
    if (product.getDiscountPersent() != null && product.getDiscountPersent() >= 0)
      curProduct.setDiscountPersent(product.getDiscountPersent());
    curProduct.updateDiscountedPrice();

    if (product.getQuantity() != null && product.getQuantity() >= 0)
      curProduct.setQuantity(product.getQuantity());

    if (product.getTopLevelCategory() != null && product.getSecondLevelCategory() != null) {
      Category parent =
          categoryRepository
              .findByNameIgnoreCase(product.getTopLevelCategory())
              .orElseThrow(() -> new EntityNotFoundException("Top level category not found"));
      Category category =
          categoryRepository
              .findByNameIgnoreCase(product.getSecondLevelCategory())
              .orElseThrow(() -> new EntityNotFoundException("Second level category not found"));
      if (parent.getLevel() != 1
          || category.getLevel() != 2
          || category.getParentCategory() == null
          || !category.getParentCategory().getId().equals(parent.getId())) {
        throw new IllegalArgumentException(
            "Second level category does not belong to top level category");
      }
      curProduct.setCategory(category);
    }
    Product updatedProduct = productRepository.save(curProduct);
    // Publish event so AI Service updates its search index
    productEventPublisher.publishProductUpdated(updatedProduct);
    return new ProductDTO(updatedProduct);
  }

  private static Map<String, Object> mapProductToMap(Product p) {
    Map<String, Object> productMap = new HashMap<>();
    productMap.put("id", p.getId());
    productMap.put("title", p.getTitle());
    productMap.put("name", p.getTitle());
    productMap.put("brand", p.getBrand());
    productMap.put("price", p.getPrice());
    productMap.put("discounted_price", p.getDiscountedPrice());
    productMap.put("discountedPrice", p.getDiscountedPrice());
    productMap.put("quantity", p.getQuantity());
    productMap.put(
        "category", p.getCategory() != null ? p.getCategory().getName() : "Uncategorized");
    long sold = p.getQuantitySold() != null ? p.getQuantitySold() : 0L;
    productMap.put("quantity_sold", sold);
    productMap.put("quantitySold", sold);
    String imgUrl =
        (p.getImages() != null && !p.getImages().isEmpty())
            ? p.getImages().get(0).getDownloadUrl()
            : null;
    productMap.put("imageUrl", imgUrl);
    return productMap;
  }

  @Transactional
  public void decreaseStock(Long productId, String sizeName, int quantity) {
    Product product =
        productRepository
            .findByIdWithLock(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

    ProductSize targetSize =
        product.getSizes().stream()
            .filter(ps -> ps.getName().equalsIgnoreCase(sizeName))
            .findFirst()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Size '" + sizeName + "' not found for product ID " + productId));

    if (targetSize.getQuantity() == null || targetSize.getQuantity() < quantity) {
      throw new RuntimeException(
          "Insufficient stock for product ID " + productId + " size " + sizeName);
    }
    targetSize.setQuantity(targetSize.getQuantity() - quantity);

    Long currentQuantitySold = product.getQuantitySold() != null ? product.getQuantitySold() : 0L;
    product.setQuantitySold(currentQuantitySold + quantity);
    productRepository.save(product);
  }

  @Transactional
  public void increaseStock(Long productId, String sizeName, int quantity) {
    Product product =
        productRepository
            .findByIdWithLock(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

    ProductSize targetSize =
        product.getSizes().stream()
            .filter(ps -> ps.getName().equalsIgnoreCase(sizeName))
            .findFirst()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Size '" + sizeName + "' not found for product ID " + productId));

    targetSize.setQuantity(
        (targetSize.getQuantity() != null ? targetSize.getQuantity() : 0) + quantity);

    Long currentQuantitySold = product.getQuantitySold() != null ? product.getQuantitySold() : 0L;
    product.setQuantitySold(Math.max(0, currentQuantitySold - quantity));
    productRepository.save(product);
  }
}
