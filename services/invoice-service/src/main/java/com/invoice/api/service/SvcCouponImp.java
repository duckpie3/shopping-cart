package com.invoice.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.coupon.CouponRequest;
import com.invoice.api.entity.Coupon;
import com.invoice.api.repository.RepoCoupon;
import com.invoice.exception.ApiException;

@Service
public class SvcCouponImp implements SvcCoupon {

	private final RepoCoupon repoCoupon;

	public SvcCouponImp(RepoCoupon repoCoupon) {
		this.repoCoupon = repoCoupon;
	}

	@Override
	public Coupon create(CouponRequest request) {
		if (repoCoupon.existsByCode(request.getCode())) {
			throw new ApiException(HttpStatus.CONFLICT, "El cupón ya existe");
		}
		return repoCoupon.save(new Coupon(null, request.getCode(), request.getDiscount(), true));
	}

	@Override
	public List<Coupon> findAll() {
		return repoCoupon.findAllByStatusTrue();
	}
}
