package com.invoice.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupon")
public class Coupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer coupon_id;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	// Porcentaje de descuento (1-100)
	@Column(nullable = false)
	private Integer discount;

	@Column(nullable = false)
	private Boolean status = true;

	public Coupon() {
	}

	public Coupon(Integer coupon_id, String code, Integer discount, Boolean status) {
		this.coupon_id = coupon_id;
		this.code = code;
		this.discount = discount;
		this.status = status;
	}

	public Integer getCoupon_id() {
		return coupon_id;
	}

	public void setCoupon_id(Integer coupon_id) {
		this.coupon_id = coupon_id;
	}

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

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}
}
