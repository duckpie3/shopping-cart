package com.invoice.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.cart.CartItemRequest;
import com.invoice.api.dto.cart.DtoCartItem;
import com.invoice.api.entity.CartItem;
import com.invoice.api.entity.Product;
import com.invoice.api.repository.RepoCartItem;
import com.invoice.api.repository.RepoProduct;
import com.invoice.commons.util.UserContext;
import com.invoice.exception.ApiException;

@Service
public class SvcCartItemImp implements SvcCartItem {

	private final RepoCartItem repoCartItem;
	private final RepoProduct repoProduct;
	private final UserContext userContext;

	public SvcCartItemImp(RepoCartItem repoCartItem, RepoProduct repoProduct, UserContext userContext) {
		this.repoCartItem = repoCartItem;
		this.repoProduct = repoProduct;
		this.userContext = userContext;
	}

	@Override
	@Transactional
	public DtoCartItem create(CartItemRequest request) {
		Integer userId = userContext.getUserId();
		Product product = findActiveProduct(request.getGtin());
		CartItem cartItem = repoCartItem.findByUserIdAndGtinAndStatusTrue(userId, request.getGtin())
				.map(existingItem -> {
					existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
					return existingItem;
				})
				.orElseGet(() -> new CartItem(null, userId, request.getGtin(), request.getQuantity(), true));

		if (product.getStock() < cartItem.getQuantity()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto " + product.getName());
		}

		return toDto(repoCartItem.save(cartItem), product);
	}

	@Override
	public List<DtoCartItem> findAll() {
		Integer userId = userContext.getUserId();
		return repoCartItem.findAllByUserIdAndStatusTrue(userId)
				.stream()
				.map(this::toDto)
				.toList();
	}

	@Override
	@Transactional
	public ApiResponse deleteById(Integer id) {
		Integer userId = userContext.getUserId();
		CartItem cartItem = repoCartItem.findByCartItemIdAndUserIdAndStatusTrue(id, userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El artículo no existe en el carrito"));
		cartItem.setStatus(false);
		repoCartItem.save(cartItem);
		return new ApiResponse("El artículo ha sido eliminado del carrito");
	}

	@Override
	@Transactional
	public ApiResponse deleteAll() {
		Integer userId = userContext.getUserId();
		List<CartItem> cartItems = repoCartItem.findAllByUserIdAndStatusTrue(userId);
		cartItems.forEach(cartItem -> cartItem.setStatus(false));
		repoCartItem.saveAll(cartItems);
		return new ApiResponse("El carrito ha sido vaciado");
	}

	private DtoCartItem toDto(CartItem cartItem) {
		Product product = findActiveProduct(cartItem.getGtin());
		return toDto(cartItem, product);
	}

	private DtoCartItem toDto(CartItem cartItem, Product product) {
		BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
		return new DtoCartItem(
				cartItem.getCart_item_id(),
				cartItem.getGtin(),
				product.getName(),
				cartItem.getQuantity(),
				product.getPrice(),
				total);
	}

	private Product findActiveProduct(String gtin) {
		return repoProduct.findByGtinAndStatusTrue(gtin)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto no existe"));
	}
}
