package com.invoice.api.service;

import java.util.List;

import com.invoice.api.dto.coupon.CouponRequest;
import com.invoice.api.entity.Coupon;

public interface SvcCoupon {

	Coupon create(CouponRequest request);
	List<Coupon> findAll();
}
