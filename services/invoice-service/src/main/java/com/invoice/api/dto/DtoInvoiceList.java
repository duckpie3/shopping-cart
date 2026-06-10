package com.invoice.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DtoInvoiceList {
	
	private Integer id;
	
	private Integer user_id;
		
	private LocalDate created_at;
	
	private BigDecimal subtotal;
	
	private BigDecimal taxes;
	
	private BigDecimal total;
	
	public DtoInvoiceList() {
		
	}

	public DtoInvoiceList(Integer id, Integer user_id, LocalDate created_at, BigDecimal subtotal, BigDecimal taxes,
			BigDecimal total) {
		super();
		this.id = id;
		this.user_id = user_id;
		this.created_at = created_at;
		this.subtotal = subtotal;
		this.taxes = taxes;
		this.total = total;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public LocalDate getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getTaxes() {
		return taxes;
	}

	public void setTaxes(BigDecimal taxes) {
		this.taxes = taxes;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	
}
