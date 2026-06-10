package com.invoice.api.dto.cart;

import java.math.BigDecimal;

public class DtoCartItem {

	private Integer id;
	private String gtin;
	private String name;
	private Integer quantity;
	private BigDecimal unit_price;
	private BigDecimal total;

	public DtoCartItem(Integer id, String gtin, String name, Integer quantity, BigDecimal unit_price, BigDecimal total) {
		this.id = id;
		this.gtin = gtin;
		this.name = name;
		this.quantity = quantity;
		this.unit_price = unit_price;
		this.total = total;
	}

	public Integer getId() {
		return id;
	}

	public String getGtin() {
		return gtin;
	}

	public String getName() {
		return name;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getUnit_price() {
		return unit_price;
	}

	public BigDecimal getTotal() {
		return total;
	}
}
