package com.invoice.api.dto.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequest {

	@NotBlank(message = "El gtin es requerido")
	@Size(max = 13, message = "El gtin no puede exceder 13 caracteres")
	private String gtin;

	@NotBlank(message = "El nombre es requerido")
	private String name;

	@NotNull(message = "El precio es requerido")
	@DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
	private BigDecimal price;

	@NotNull(message = "El stock es requerido")
	@Min(value = 0, message = "El stock no puede ser negativo")
	private Integer stock;

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}
}
