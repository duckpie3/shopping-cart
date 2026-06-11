package com.invoice.api.dto;

import jakarta.validation.constraints.Size;

/**
 * Información opcional al finalizar la compra (puntos extra):
 * dirección de envío, información de pago y cupón de descuento.
 */
public class CheckoutRequest {

	@Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
	private String shippingAddress;

	@Size(max = 20, message = "El método de pago no puede exceder 20 caracteres")
	private String paymentMethod;

	@Size(min = 4, max = 4, message = "Los últimos dígitos de la tarjeta deben ser 4")
	private String cardLast4;

	@Size(max = 20, message = "El cupón no puede exceder 20 caracteres")
	private String couponCode;

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getCardLast4() {
		return cardLast4;
	}

	public void setCardLast4(String cardLast4) {
		this.cardLast4 = cardLast4;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
}
