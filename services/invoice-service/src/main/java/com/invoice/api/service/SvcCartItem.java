package com.invoice.api.service;

import java.util.List;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.cart.CartItemRequest;
import com.invoice.api.dto.cart.DtoCartItem;

public interface SvcCartItem {

	DtoCartItem create(CartItemRequest request);
	List<DtoCartItem> findAll();
	ApiResponse deleteById(Integer id);
	ApiResponse deleteAll();
}
