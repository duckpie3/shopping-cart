package com.invoice.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.product.ProductRequest;
import com.invoice.api.entity.Product;
import com.invoice.api.service.SvcProduct;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/product")
@Tag(name = "Product", description = "Administración de productos")
public class CtrlProduct {

	private final SvcProduct svcProduct;

	public CtrlProduct(SvcProduct svcProduct) {
		this.svcProduct = svcProduct;
	}

	@PostMapping
	@Operation(summary = "Creación de producto")
	public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.ok(svcProduct.create(request));
	}

	@GetMapping
	@Operation(summary = "Consulta de productos")
	public ResponseEntity<List<Product>> findAll() {
		return ResponseEntity.ok(svcProduct.findAll());
	}

	@GetMapping("/{gtin}")
	@Operation(summary = "Consulta de producto")
	public ResponseEntity<Product> findByGtin(@PathVariable String gtin) {
		return ResponseEntity.ok(svcProduct.findByGtin(gtin));
	}

	@PutMapping("/{gtin}")
	@Operation(summary = "Actualización de producto")
	public ResponseEntity<Product> update(@PathVariable String gtin, @Valid @RequestBody ProductRequest request) {
		return ResponseEntity.ok(svcProduct.update(gtin, request));
	}

	@DeleteMapping("/{gtin}")
	@Operation(summary = "Eliminación de producto")
	public ResponseEntity<ApiResponse> delete(@PathVariable String gtin) {
		return ResponseEntity.ok(svcProduct.delete(gtin));
	}
}
