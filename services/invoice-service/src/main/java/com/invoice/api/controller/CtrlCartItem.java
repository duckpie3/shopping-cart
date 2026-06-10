package com.invoice.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.cart.CartItemRequest;
import com.invoice.api.dto.cart.DtoCartItem;
import com.invoice.api.service.SvcCartItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart-item")
@Tag(name = "Cart Item", description = "Administración del carrito de compras")
public class CtrlCartItem {

	private final SvcCartItem svcCartItem;

	public CtrlCartItem(SvcCartItem svcCartItem) {
		this.svcCartItem = svcCartItem;
	}

	@PostMapping
	@Operation(summary = "Agregar producto al carrito")
	public ResponseEntity<DtoCartItem> create(@Valid @RequestBody CartItemRequest request) {
		return ResponseEntity.ok(svcCartItem.create(request));
	}

	@GetMapping
	@Operation(summary = "Consultar carrito")
	public ResponseEntity<List<DtoCartItem>> findAll() {
		return ResponseEntity.ok(svcCartItem.findAll());
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar artículo del carrito")
	public ResponseEntity<ApiResponse> deleteById(@PathVariable Integer id) {
		return ResponseEntity.ok(svcCartItem.deleteById(id));
	}

	@DeleteMapping
	@Operation(summary = "Vaciar carrito")
	public ResponseEntity<ApiResponse> deleteAll() {
		return ResponseEntity.ok(svcCartItem.deleteAll());
	}
}
