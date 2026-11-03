package com.example.campusshop.product;

import java.util.List;

import com.example.campusshop.product.dto.ProductRequest;
import com.example.campusshop.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<ProductResponse> findAll(String keyword, String category) {
        return productMapper.findAll(trimToNull(keyword), trimToNull(category)).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productMapper.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = toEntity(request);
        productMapper.insert(product);
        return findById(product.getId());
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        if (productMapper.findById(id).isEmpty()) {
            throw new ProductNotFoundException(id);
        }

        Product product = toEntity(request);
        product.setId(id);
        productMapper.update(product);
        return findById(id);
    }

    @Transactional
    public void delete(Long id) {
        if (productMapper.deleteById(id) == 0) {
            throw new ProductNotFoundException(id);
        }
    }

    private Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name().trim());
        product.setDescription(request.description() == null ? "" : request.description().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category().trim());
        return product;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
