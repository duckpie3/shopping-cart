package com.invoice.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invoice.api.dto.coupon.CouponRequest;
import com.invoice.api.entity.Coupon;
import com.invoice.api.service.SvcCoupon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/coupon")
@Tag(name = "Coupon", description = "Administración de cupones de descuento")
public class CtrlCoupon {

	private final SvcCoupon svcCoupon;

	public CtrlCoupon(SvcCoupon svcCoupon) {
		this.svcCoupon = svcCoupon;
	}

	@PostMapping
	@Operation(summary = "Creación de cupón")
	public ResponseEntity<Coupon> create(@Valid @RequestBody CouponRequest request) {
		return ResponseEntity.ok(svcCoupon.create(request));
	}

	@GetMapping
	@Operation(summary = "Consulta de cupones")
	public ResponseEntity<List<Coupon>> findAll() {
		return ResponseEntity.ok(svcCoupon.findAll());
	}
}
