package com.invoice.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.invoice.api.entity.Invoice;
import com.invoice.api.repository.RepoCartItem;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoProduct;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ShoppingFlowIntegrationTests {

	private static final String CUSTOMER_ID = "10";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepoProduct repoProduct;

	@Autowired
	private RepoCartItem repoCartItem;

	@Autowired
	private RepoInvoice repoInvoice;

	@BeforeEach
	void cleanDatabase() {
		repoInvoice.deleteAll();
		repoCartItem.deleteAll();
		repoProduct.deleteAll();
	}

	@Test
	void productCrudUpdatesAndDeletes() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);

		mockMvc.perform(get("/product"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is("Coca-cola 600 ml")));

		mockMvc.perform(put("/product/7501055300075")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "gtin": "7501055300075",
						  "name": "Coca-cola 600 ml retornable",
						  "price": 23.50,
						  "stock": 80
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Coca-cola 600 ml retornable")))
				.andExpect(jsonPath("$.stock", is(80)));

		mockMvc.perform(delete("/product/7501055300075"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("El producto ha sido eliminado")));

		mockMvc.perform(get("/product"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void cartLifecycleUsesCustomerHeader() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);

		mockMvc.perform(post("/cart-item")
				.header("X-Customer-Id", CUSTOMER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "gtin": "7501055300075",
						  "quantity": 2
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Coca-cola 600 ml")))
				.andExpect(jsonPath("$.quantity", is(2)))
				.andExpect(jsonPath("$.total", is(42.0)));

		Integer cartItemId = repoCartItem.findAllByUserIdAndStatusTrue(Integer.valueOf(CUSTOMER_ID)).get(0).getCart_item_id();

		mockMvc.perform(get("/cart-item").header("X-Customer-Id", CUSTOMER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(delete("/cart-item/{id}", cartItemId).header("X-Customer-Id", CUSTOMER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("El artículo ha sido eliminado del carrito")));

		mockMvc.perform(get("/cart-item").header("X-Customer-Id", CUSTOMER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void checkoutCreatesInvoiceUpdatesStockAndClearsCart() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);

		addCartItem("7501055300075", 2);

		mockMvc.perform(post("/invoice").header("X-Customer-Id", CUSTOMER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("La factura ha sido registrada")));

		Invoice invoice = repoInvoice.findAll().get(0);
		BigDecimal expectedTotal = new BigDecimal("42.00");
		BigDecimal expectedTaxes = new BigDecimal("6.72");
		BigDecimal expectedSubtotal = new BigDecimal("35.28");

		org.assertj.core.api.Assertions.assertThat(invoice.getTotal()).isEqualByComparingTo(expectedTotal);
		org.assertj.core.api.Assertions.assertThat(invoice.getTaxes()).isEqualByComparingTo(expectedTaxes);
		org.assertj.core.api.Assertions.assertThat(invoice.getSubtotal()).isEqualByComparingTo(expectedSubtotal);
		org.assertj.core.api.Assertions.assertThat(invoice.getItems()).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(repoProduct.findById("7501055300075").get().getStock()).isEqualTo(98);
		org.assertj.core.api.Assertions.assertThat(repoCartItem.findAllByUserIdAndStatusTrue(Integer.valueOf(CUSTOMER_ID))).isEmpty();
	}

	@Test
	void checkoutRejectsInsufficientStock() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 1);

		addCartItem("7501055300075", 2);

		mockMvc.perform(post("/invoice").header("X-Customer-Id", CUSTOMER_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Stock insuficiente para el producto Coca-cola 600 ml")));
	}

	private void createProduct(String gtin, String name, String price, int stock) throws Exception {
		mockMvc.perform(post("/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "gtin": "%s",
						  "name": "%s",
						  "price": %s,
						  "stock": %d
						}
						""".formatted(gtin, name, price, stock)))
				.andExpect(status().isOk());
	}

	private void addCartItem(String gtin, int quantity) throws Exception {
		mockMvc.perform(post("/cart-item")
				.header("X-Customer-Id", CUSTOMER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "gtin": "%s",
						  "quantity": %d
						}
						""".formatted(gtin, quantity)))
				.andExpect(status().isOk());
	}
}
