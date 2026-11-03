package com.example.campusshop.product;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper {

    List<Product> findAll(@Param("keyword") String keyword, @Param("category") String category);

    Optional<Product> findById(@Param("id") Long id);

    void insert(Product product);

    int update(Product product);

    int deleteById(@Param("id") Long id);
}
