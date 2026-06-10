package com.invoice.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataAccessException;
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
import com.invoice.commons.util.CustomerContext;
import com.invoice.commons.util.JwtDecoder;
import com.invoice.exception.ApiException;
import com.invoice.exception.DBAccessException;

@Service
public class SvcInvoiceImp implements SvcInvoice {
	
	private static final BigDecimal TAX_RATE = new BigDecimal("0.16");

    private final RepoInvoice repo;
	private final RepoCartItem repoCartItem;
	private final RepoProduct repoProduct;
	private final CustomerContext customerContext;
	private final JwtDecoder jwtDecoder;
	private final MapperInvoice mapper;

	public SvcInvoiceImp(
			RepoInvoice repo,
			RepoCartItem repoCartItem,
			RepoProduct repoProduct,
			CustomerContext customerContext,
			JwtDecoder jwtDecoder,
			MapperInvoice mapper) {
		this.repo = repo;
		this.repoCartItem = repoCartItem;
		this.repoProduct = repoProduct;
		this.customerContext = customerContext;
		this.jwtDecoder = jwtDecoder;
		this.mapper = mapper;
	}

	@Override
	public List<DtoInvoiceList> findAll() {
		try {
			if (jwtDecoder.isAdmin()) {
				return mapper.toDtoList(repo.findAll());
			}
			Integer user_id = customerContext.getCustomerId();
			return mapper.toDtoList(repo.findAllByUserId(user_id));
		}catch (DataAccessException e) {
	        throw new DBAccessException();
	    }
	}

	@Override
	public Invoice findById(Integer id) {
		try {
			Invoice invoice = repo.findById(id).get();
			if (jwtDecoder.isAdmin()) {
				return invoice;
			}
			Integer user_id = customerContext.getCustomerId();
			if(!invoice.getUser_id().equals(user_id)) {
				throw new ApiException(HttpStatus.FORBIDDEN, "El cliente no puede consultar esta factura");
			}
			return invoice;
		}catch (DataAccessException e) {
	        throw new DBAccessException();
	    }catch (NoSuchElementException e) {
			throw new ApiException(HttpStatus.NOT_FOUND, "El id de la factura no existe");
	    }
	}

	@Override
	@Transactional
	public ApiResponse create() {
		try {
			Integer userId = customerContext.getCustomerId();
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

				BigDecimal itemTotal = product.getPrice()
						.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
						.setScale(2, RoundingMode.HALF_UP);
				BigDecimal itemTaxes = itemTotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
				BigDecimal itemSubtotal = itemTotal.subtract(itemTaxes).setScale(2, RoundingMode.HALF_UP);

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

			invoice.setTotal(invoiceTotal.setScale(2, RoundingMode.HALF_UP));
			invoice.setTaxes(invoiceTaxes.setScale(2, RoundingMode.HALF_UP));
			invoice.setSubtotal(invoiceSubtotal.setScale(2, RoundingMode.HALF_UP));
			invoice.setItems(invoiceItems);

			repo.save(invoice);
			repoCartItem.saveAll(cartItems);

			return new ApiResponse("La factura ha sido registrada");
		}catch (DataAccessException e) {
	        throw new DBAccessException();
	    }
	}
}
