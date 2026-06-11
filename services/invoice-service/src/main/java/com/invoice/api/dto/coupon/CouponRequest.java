package com.invoice.api.dto.coupon;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CouponRequest {

	@NotBlank(message = "El código es requerido")
	@Size(max = 20, message = "El código no puede exceder 20 caracteres")
	private String code;

	@NotNull(message = "El descuento es requerido")
	@Min(value = 1, message = "El descuento debe ser al menos 1%")
	@Max(value = 100, message = "El descuento no puede exceder 100%")
	private Integer discount;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
}
