package com.invoice.api.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CartItemRequest {

	@NotBlank(message = "El gtin es requerido")
	@Size(max = 13, message = "El gtin no puede exceder 13 caracteres")
	private String gtin;

	@NotNull(message = "La cantidad es requerida")
	@Min(value = 1, message = "La cantidad debe ser mayor a cero")
	private Integer quantity;

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
