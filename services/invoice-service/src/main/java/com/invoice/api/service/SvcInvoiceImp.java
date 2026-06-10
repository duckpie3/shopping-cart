package com.invoice.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoInvoiceList;
import com.invoice.api.entity.CartItem;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.InvoiceItem;
import com.invoice.api.entity.Product;
import com.invoice.api.repository.RepoCartItem;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoProduct;
import com.invoice.commons.mapper.MapperInvoice;
import com.invoice.commons.util.UserContext;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	private static final BigDecimal TAX_RATE = new BigDecimal("0.16");

	private final RepoInvoice repo;
	private final RepoCartItem repoCartItem;
	private final RepoProduct repoProduct;
	private final UserContext userContext;
	private final MapperInvoice mapper;

	public SvcInvoiceImp(
			RepoInvoice repo,
			RepoCartItem repoCartItem,
			RepoProduct repoProduct,
			UserContext userContext,
			MapperInvoice mapper) {
		this.repo = repo;
		this.repoCartItem = repoCartItem;
		this.repoProduct = repoProduct;
		this.userContext = userContext;
		this.mapper = mapper;
	}

	@Override
	public List<DtoInvoiceList> findAll() {
		if (userContext.isAdmin()) {
			return mapper.toDtoList(repo.findAll());
		}
		return mapper.toDtoList(repo.findAllByUserId(userContext.getUserId()));
	}

	@Override
	public Invoice findById(Integer id) {
		Invoice invoice = repo.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El id de la factura no existe"));
		if (!userContext.isAdmin() && !invoice.getUser_id().equals(userContext.getUserId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "El cliente no puede consultar esta factura");
		}
		return invoice;
	}

	@Override
	@Transactional
	public ApiResponse create() {
		Integer userId = userContext.getUserId();
		List<CartItem> cartItems = repoCartItem.findAllByUserIdAndStatusTrue(userId);
		if (cartItems.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito está vacío");
		}

		Invoice invoice = new Invoice();
		invoice.setUser_id(userId);
		invoice.setCreated_at(LocalDate.now());
		invoice.setStatus(true);

		List<InvoiceItem> invoiceItems = new ArrayList<>();
		BigDecimal invoiceTotal = BigDecimal.ZERO;
		BigDecimal invoiceTaxes = BigDecimal.ZERO;
		BigDecimal invoiceSubtotal = BigDecimal.ZERO;

		for (CartItem cartItem : cartItems) {
			Product product = repoProduct.findByGtinAndStatusTrue(cartItem.getGtin())
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto " + cartItem.getGtin() + " no existe"));

			if (product.getStock() < cartItem.getQuantity()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto " + product.getName());
			}

			// Según la especificación: impuestos = total * 0.16, subtotal = total - impuestos
			BigDecimal itemTotal = product.getPrice()
					.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
					.setScale(2, RoundingMode.HALF_UP);
			BigDecimal itemTaxes = itemTotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
			BigDecimal itemSubtotal = itemTotal.subtract(itemTaxes);

			InvoiceItem invoiceItem = new InvoiceItem();
			invoiceItem.setInvoice(invoice);
			invoiceItem.setGtin(product.getGtin());
			invoiceItem.setQuantity(cartItem.getQuantity());
			invoiceItem.setUnit_price(product.getPrice());
			invoiceItem.setSubtotal(itemSubtotal);
			invoiceItem.setTaxes(itemTaxes);
			invoiceItem.setTotal(itemTotal);
			invoiceItem.setStatus(true);
			invoiceItems.add(invoiceItem);

			product.setStock(product.getStock() - cartItem.getQuantity());
			repoProduct.save(product);

			cartItem.setStatus(false);

			invoiceTotal = invoiceTotal.add(itemTotal);
			invoiceTaxes = invoiceTaxes.add(itemTaxes);
			invoiceSubtotal = invoiceSubtotal.add(itemSubtotal);
		}

		invoice.setTotal(invoiceTotal);
		invoice.setTaxes(invoiceTaxes);
		invoice.setSubtotal(invoiceSubtotal);
		invoice.setItems(invoiceItems);

		repo.save(invoice);
		repoCartItem.saveAll(cartItems);

		return new ApiResponse("La factura ha sido registrada");
	}
}
