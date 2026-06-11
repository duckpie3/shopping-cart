package com.invoice.api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoice")
public class Invoice {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer invoice_id;

	@Column(nullable = false)
	private Integer user_id;
	
	@Column(nullable = false)
	private LocalDate created_at;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal taxes;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(nullable = false)
	private Boolean status = true;

	// Puntos extra: dirección de envío, información de pago y cupón de descuento
	@Column(length = 255)
	private String shipping_address;

	@Column(length = 20)
	private String payment_method;

	@Column(length = 4)
	private String card_last4;

	@Column(length = 20)
	private String coupon_code;

	@Column(precision = 12, scale = 2)
	private BigDecimal discount;

	@OneToMany(mappedBy = "invoice", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoiceItem> items;
	
	public Invoice() {
		
	}

	public Invoice(Integer invoice_id, Integer user_id, LocalDate created_at, BigDecimal subtotal, BigDecimal taxes,
			BigDecimal total, Boolean status, List<InvoiceItem> items) {
		super();
		this.invoice_id = invoice_id;
		this.user_id = user_id;
		this.created_at = created_at;
		this.subtotal = subtotal;
		this.taxes = taxes;
		this.total = total;
		this.status = status;
		this.items = items;
	}

	public Integer getInvoice_id() {
		return invoice_id;
	}

	public void setInvoice_id(Integer invoice_id) {
		this.invoice_id = invoice_id;
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

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getShipping_address() {
		return shipping_address;
	}

	public void setShipping_address(String shipping_address) {
		this.shipping_address = shipping_address;
	}

	public String getPayment_method() {
		return payment_method;
	}

	public void setPayment_method(String payment_method) {
		this.payment_method = payment_method;
	}

	public String getCard_last4() {
		return card_last4;
	}

	public void setCard_last4(String card_last4) {
		this.card_last4 = card_last4;
	}

	public String getCoupon_code() {
		return coupon_code;
	}

	public void setCoupon_code(String coupon_code) {
		this.coupon_code = coupon_code;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public List<InvoiceItem> getItems() {
		return items;
	}

	public void setItems(List<InvoiceItem> items) {
		this.items = items;
	}

	
}
