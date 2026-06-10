package com.invoice.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.product.ProductRequest;
import com.invoice.api.entity.Product;
import com.invoice.api.repository.RepoProduct;
import com.invoice.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

	private final RepoProduct repoProduct;

	public SvcProductImp(RepoProduct repoProduct) {
		this.repoProduct = repoProduct;
	}

	@Override
	public Product create(ProductRequest request) {
		if (repoProduct.existsById(request.getGtin())) {
			throw new ApiException(HttpStatus.CONFLICT, "El producto ya existe");
		}
		Product product = new Product(request.getGtin(), request.getName(), request.getPrice(), request.getStock(), true);
		return repoProduct.save(product);
	}

	@Override
	public List<Product> findAll() {
		return repoProduct.findAllByStatusTrue();
	}

	@Override
	public Product findByGtin(String gtin) {
		return findActiveProduct(gtin);
	}

	@Override
	public Product update(String gtin, ProductRequest request) {
		Product product = findActiveProduct(gtin);
		product.setName(request.getName());
		product.setPrice(request.getPrice());
		product.setStock(request.getStock());
		return repoProduct.save(product);
	}

	@Override
	public ApiResponse delete(String gtin) {
		Product product = findActiveProduct(gtin);
		product.setStatus(false);
		repoProduct.save(product);
		return new ApiResponse("El producto ha sido eliminado");
	}

	private Product findActiveProduct(String gtin) {
		return repoProduct.findByGtinAndStatusTrue(gtin)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto no existe"));
	}
}
