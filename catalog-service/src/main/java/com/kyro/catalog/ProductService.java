package com.kyro.catalog;

import com.kyro.catalog.dto.CreateProductRequest;
import com.kyro.catalog.dto.ProductDTO;
import com.kyro.catalog.messaging.ProductEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

  public ProductService(
      ProductRepository productRepository,
      CategoryRepository categoryRepository,
      ReviewRepository reviewRepository,
      ImageService imageService,
      ProductEventPublisher productEventPublisher) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
    this.reviewRepository = reviewRepository;
    this.imageService = imageService;
    this.productEventPublisher = productEventPublisher;
  }

  @Transactional
  public Product createProduct(CreateProductRequest req) {
    // Logic for handling categories
    Category parentCategory = null;
    Category category = null;

    // Handle top level category
    if (req.getTopLevelCategory() != null && !req.getTopLevelCategory().isEmpty()) {
      parentCategory = categoryRepository.findByName(req.getTopLevelCategory());
      if (parentCategory == null) {
        parentCategory = new Category();
        parentCategory.setName(req.getTopLevelCategory());
        parentCategory.setLevel(1);
        parentCategory.setParent(true);
        parentCategory = categoryRepository.save(parentCategory);
      } else if (parentCategory.getLevel() != 1) {
        throw new IllegalArgumentException("Top level category must have level 1");
      }

      // Handle second level category if provided
      if (req.getSecondLevelCategory() != null && !req.getSecondLevelCategory().isEmpty()) {
        category = categoryRepository.findByName(req.getSecondLevelCategory());
        if (category == null) {
          category = new Category();
          category.setName(req.getSecondLevelCategory());
          category.setLevel(2);
          category.setParent(false);
          category.setParentCategory(parentCategory);
          category = categoryRepository.save(category);
        } else if (category.getLevel() != 2) {
          throw new IllegalArgumentException("Second level category must have level 2");
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

    // Handle images if provided
    if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
      for (Image imageUrl : req.getImageUrls()) {
        imageUrl.setProduct(product);
      }
      product.setImages(req.getImageUrls());
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

  public List<Product> findAllProducts() {
    return productRepository.findAll();
  }

  @Transactional
  public List<ProductDTO> getAllProducts(
      String search, String categoryName, String sort, String order) {
    List<Product> products;

    if (search != null && !search.isEmpty()) {
      products = productRepository.searchProducts(search);
    } else if (categoryName != null && !categoryName.isEmpty()) {
      Category category = categoryRepository.findByName(categoryName);

      if (category != null) {
        products = productRepository.findByCategory(category);
      } else {
        products = new ArrayList<>();
      }
    } else {
      products = productRepository.findAll();
    }

    if (sort != null && order != null) {
      sortProducts(products, sort, order);
    }

    return products.stream().map(ProductDTO::new).toList();
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

  public List<Product> findByCategoryTopAndSecond(String topCategory, String secondCategory) {
    return productRepository.findProductsByTopAndSecondCategoryNames(topCategory, secondCategory);
  }

  @Transactional(readOnly = true)
  public Page<ProductDTO> getProductsWithFilter(
      Pageable pageable, FilterProduct filter, String status) {

    Boolean inStock = null;
    if (status != null && !status.equals("all")) {
      switch (status) {
        case "inStock":
          inStock = true;
          break;
        case "outOfStock":
          inStock = false;
          break;
      }
    }

    Pageable finalPageable = pageable;
    if (filter != null && filter.getSort() != null && !filter.getSort().isEmpty()) {
      finalPageable = applySorting(pageable, filter.getSort());
    }

    Page<Product> productPage =
        productRepository.getProductsWithFilter(
            filter != null ? filter.getKeyword() : null,
            filter != null ? filter.getTopLevelCategory() : null,
            filter != null ? filter.getSecondLevelCategory() : null,
            filter != null ? filter.getColor() : null,
            filter != null ? filter.getMinPrice() : null,
            filter != null ? filter.getMaxPrice() : null,
            inStock,
            finalPageable);

    return productPage.map(ProductDTO::new);
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
    long totalSoldItems = allProducts.stream()
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

  @Transactional(readOnly = true)
  public List<Product> findAllProductsByFilter(FilterProduct filter) {
    Specification<Product> spec =
        (root, query, criteriaBuilder) -> {
          List<Predicate> predicates = new ArrayList<>();

          // Filter by keyword
          if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + filter.getKeyword().toLowerCase() + "%"));
          }

          // Filter by color
          if (filter.getColor() != null && !filter.getColor().isEmpty()) {
            predicates.add(
                criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("color")), filter.getColor().toLowerCase()));
          }

          // Filter by price range
          if (filter.getMinPrice() != null) {
            predicates.add(
                criteriaBuilder.greaterThanOrEqualTo(
                    root.get("discountedPrice"), filter.getMinPrice()));
          }
          if (filter.getMaxPrice() != null) {
            predicates.add(
                criteriaBuilder.lessThanOrEqualTo(
                    root.get("discountedPrice"), filter.getMaxPrice()));
          }

          // --- UPDATED CATEGORY AND BRAND FILTER LOGIC ---
          boolean needsCategoryJoin =
              (filter.getTopLevelCategory() != null && !filter.getTopLevelCategory().isEmpty())
                  || (filter.getBrand() != null && !filter.getBrand().isEmpty())
                  || (filter.getSecondLevelCategory() != null
                      && !filter.getSecondLevelCategory().isEmpty());

          if (needsCategoryJoin) {
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);

            // Filter by Top-Level Category OR Parent of a Brand/Second-Level Category
            if (filter.getTopLevelCategory() != null && !filter.getTopLevelCategory().isEmpty()) {
              Join<Category, Category> parentCategoryJoin =
                  categoryJoin.join("parentCategory", JoinType.LEFT);
              Predicate topLevelDirect =
                  criteriaBuilder.equal(
                      criteriaBuilder.lower(categoryJoin.get("name")),
                      filter.getTopLevelCategory().toLowerCase());
              Predicate parentOfChild =
                  criteriaBuilder.equal(
                      criteriaBuilder.lower(parentCategoryJoin.get("name")),
                      filter.getTopLevelCategory().toLowerCase());
              predicates.add(criteriaBuilder.or(topLevelDirect, parentOfChild));
            }

            // Filter by Brand (which is a Level 2 Category)
            if (filter.getBrand() != null && !filter.getBrand().isEmpty()) {
              predicates.add(
                  criteriaBuilder.equal(
                      criteriaBuilder.lower(categoryJoin.get("name")),
                      filter.getBrand().toLowerCase()));
            }

            // Filter by Second-Level Category (if brand is not already doing it)
            if (filter.getSecondLevelCategory() != null
                && !filter.getSecondLevelCategory().isEmpty()) {
              predicates.add(
                  criteriaBuilder.equal(
                      criteriaBuilder.lower(categoryJoin.get("name")),
                      filter.getSecondLevelCategory().toLowerCase()));
            }
          }
          // --- END OF UPDATE ---

          return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    Sort sort = Sort.unsorted();
    if (filter.getSort() != null && !filter.getSort().isEmpty()) {
      switch (filter.getSort().toLowerCase()) {
        case "price_low":
          sort = Sort.by(Sort.Direction.ASC, "discountedPrice");
          break;
        case "price_high":
          sort = Sort.by(Sort.Direction.DESC, "discountedPrice");
          break;
        case "discount":
          sort = Sort.by(Sort.Direction.DESC, "discountPersent");
          break;
        case "newest":
          sort = Sort.by(Sort.Direction.DESC, "createdAt");
          break;
      }
    }

    return productRepository.findAll(spec, sort);
  }

  public List<Map<String, Object>> getTopSellingProducts(int limit) {
    List<Map<String, Object>> result = new ArrayList<>();
    Pageable pageable = PageRequest.of(0, limit);
    List<Product> products = productRepository.findTopSellingProducts(pageable);

    for (Product p : products) {
      result.add(mapProductToMap(p));
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

    if (product.getImages() != null && !product.getImages().isEmpty()) {
      existingProduct.getImages().clear();
      for (Image image : product.getImages()) {
        image.setProduct(existingProduct);
        existingProduct.getImages().add(image);
      }
    }

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
  public ProductDTO updateProductByID(Long productId, Product product) {
    Product curProduct = findProductById(productId);

    if (product.getTitle() != null) curProduct.setTitle(product.getTitle());
    if (product.getDescription() != null) curProduct.setDescription(product.getDescription());
    if (product.getBrand() != null) curProduct.setBrand(product.getBrand());
    if (product.getColor() != null) curProduct.setColor(product.getColor());

    if (product.getImages() != null && !product.getImages().isEmpty()) {
      curProduct.getImages().clear();
      for (Image image : product.getImages()) {
        image.setProduct(curProduct);
        curProduct.getImages().add(image);
      }
    }

    if (product.getPrice() > 0) curProduct.setPrice(product.getPrice());
    if (product.getDiscountPersent() >= 0)
      curProduct.setDiscountPersent(product.getDiscountPersent());
    curProduct.updateDiscountedPrice();

    if (product.getQuantity() >= 0) curProduct.setQuantity(product.getQuantity());

    if (product.getCategory() != null && product.getCategory().getId() != null) {
      Category category =
          categoryRepository
              .findById(product.getCategory().getId())
              .orElseThrow(() -> new EntityNotFoundException("Category not found"));
      curProduct.setCategory(category);
    }
    Product updatedProduct = productRepository.save(curProduct);
    // Publish event so AI Service updates its search index
    productEventPublisher.publishProductUpdated(updatedProduct);
    return new ProductDTO(updatedProduct);
  }

  private Map<String, Object> mapProductToMap(Product p) {
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
    String imgUrl = (p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().get(0).getDownloadUrl() : null;
    productMap.put("imageUrl", imgUrl);
    return productMap;
  }

  private void sortProducts(List<Product> products, String sortBy, String order) {
    Comparator<Product> comparator = null;

    switch (sortBy) {
      case "price":
        comparator = Comparator.comparing(Product::getPrice);
        break;
      case "createdAt":
        comparator = Comparator.comparing(Product::getCreatedAt);
        break;
      case "quantitySold":
        comparator = Comparator.comparing(Product::getQuantitySold);
        break;
      case "quantity":
        comparator = Comparator.comparing(Product::getQuantity);
        break;
      default:
        return;
    }

    if ("desc".equalsIgnoreCase(order)) {
      comparator = comparator.reversed();
    }

    products.sort(comparator);
  }

  private Pageable applySorting(Pageable pageable, String sortType) {
    Sort sort;
    switch (sortType) {
      case "price_low":
        sort = Sort.by(Sort.Direction.ASC, "discountedPrice");
        break;
      case "price_high":
        sort = Sort.by(Sort.Direction.DESC, "discountedPrice");
        break;
      case "discount":
        sort = Sort.by(Sort.Direction.DESC, "discountPersent");
        break;
      case "newest":
        sort = Sort.by(Sort.Direction.DESC, "createdAt");
        break;
      case "name_asc":
        sort = Sort.by(Sort.Direction.ASC, "title");
        break;
      case "name_desc":
        sort = Sort.by(Sort.Direction.DESC, "title");
        break;
      default:
        return pageable;
    }

    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
  }

  @Transactional
  public void decreaseStock(Long productId, String sizeName, int quantity) {
    Product product =
        productRepository
            .findById(productId)
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
            .findById(productId)
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
