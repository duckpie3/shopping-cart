package com.invoice.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_item")
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer cart_item_id;

	@Column(nullable = false)
	private Integer user_id;

	@Column(nullable = false, length = 13)
	private String gtin;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Boolean status = true;

	public CartItem() {
	}

	public CartItem(Integer cart_item_id, Integer user_id, String gtin, Integer quantity, Boolean status) {
		this.cart_item_id = cart_item_id;
		this.user_id = user_id;
		this.gtin = gtin;
		this.quantity = quantity;
		this.status = status;
	}

	public Integer getCart_item_id() {
		return cart_item_id;
	}

	public void setCart_item_id(Integer cart_item_id) {
		this.cart_item_id = cart_item_id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

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

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}
}
