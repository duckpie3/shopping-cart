package com.invoice.api.service;

import java.util.List;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.product.ProductRequest;
import com.invoice.api.entity.Product;

public interface SvcProduct {

	Product create(ProductRequest request);
	List<Product> findAll();
	Product findByGtin(String gtin);
	Product update(String gtin, ProductRequest request);
	ApiResponse delete(String gtin);
}
