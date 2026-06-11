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
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.invoice.api.entity.Invoice;
import com.invoice.api.repository.RepoCartItem;
import com.invoice.api.repository.RepoCoupon;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoProduct;

import io.jsonwebtoken.Jwts;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:db_test;MODE=MySQL;DATABASE_TO_UPPER=false",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"jwt.secret=" + ShoppingFlowIntegrationTests.SECRET
})
@AutoConfigureMockMvc
class ShoppingFlowIntegrationTests {

	static final String SECRET = "8J+YjvCfpJPwn5ic8J+YmvCfmI3wn6Ww8J+ZgvCfpKM=";
	private static final SecretKey KEY = new SecretKeySpec(Base64.getDecoder().decode(SECRET), "HmacSHA256");

	private static final Integer CUSTOMER_ID = 10;
	private static final String ADMIN_TOKEN = bearer("admin", 1, "ADMIN");
	private static final String CUSTOMER_TOKEN = bearer("customer", CUSTOMER_ID, "CUSTOMER");

	private static String bearer(String username, int userId, String authority) {
		return "Bearer " + Jwts.builder()
				.setSubject(username)
				.claim("id", userId)
				.claim("roles", List.of(Map.of("authority", authority)))
				.signWith(KEY)
				.compact();
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepoProduct repoProduct;

	@Autowired
	private RepoCartItem repoCartItem;

	@Autowired
	private RepoInvoice repoInvoice;

	@Autowired
	private RepoCoupon repoCoupon;

	@BeforeEach
	void cleanDatabase() {
		repoInvoice.deleteAll();
		repoCartItem.deleteAll();
		repoProduct.deleteAll();
		repoCoupon.deleteAll();
	}

	@Test
	void productCrudUpdatesAndDeletes() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);

		mockMvc.perform(get("/product").header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is("Coca-cola 600 ml")));

		mockMvc.perform(put("/product/7501055300075")
				.header("Authorization", ADMIN_TOKEN)
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

		mockMvc.perform(delete("/product/7501055300075").header("Authorization", ADMIN_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("El producto ha sido eliminado")));

		mockMvc.perform(get("/product").header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void cartLifecycle() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);

		mockMvc.perform(post("/cart-item")
				.header("Authorization", CUSTOMER_TOKEN)
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

		// Agregar el mismo producto suma cantidades en lugar de crear otro registro
		addCartItem("7501055300075", 3);

		mockMvc.perform(get("/cart-item").header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].quantity", is(5)));

		Integer cartItemId = repoCartItem.findAllByUserIdAndStatusTrue(CUSTOMER_ID).get(0).getCart_item_id();

		mockMvc.perform(delete("/cart-item/{id}", cartItemId).header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("El artículo ha sido eliminado del carrito")));

		mockMvc.perform(get("/cart-item").header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void cartRejectsInsufficientStock() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 1);

		mockMvc.perform(post("/cart-item")
				.header("Authorization", CUSTOMER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "gtin": "7501055300075",
						  "quantity": 2
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Stock insuficiente para el producto Coca-cola 600 ml")));
	}

	@Test
	void checkoutCreatesInvoiceUpdatesStockAndClearsCart() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);

		addCartItem("7501055300075", 2);

		mockMvc.perform(post("/invoice").header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("La factura ha sido registrada")));

		// Ejemplo de la especificación: total 42.00, impuestos 6.72, subtotal 35.28
		Invoice invoice = repoInvoice.findAll().get(0);
		org.assertj.core.api.Assertions.assertThat(invoice.getTotal()).isEqualByComparingTo(new BigDecimal("42.00"));
		org.assertj.core.api.Assertions.assertThat(invoice.getTaxes()).isEqualByComparingTo(new BigDecimal("6.72"));
		org.assertj.core.api.Assertions.assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("35.28"));
		org.assertj.core.api.Assertions.assertThat(invoice.getItems()).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(repoProduct.findById("7501055300075").get().getStock()).isEqualTo(98);
		org.assertj.core.api.Assertions.assertThat(repoCartItem.findAllByUserIdAndStatusTrue(CUSTOMER_ID)).isEmpty();
	}

	@Test
	void checkoutRejectsInsufficientStock() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 5);

		addCartItem("7501055300075", 5);
		// El stock baja a 2 después de agregar al carrito; el checkout debe detectarlo
		repoProduct.findById("7501055300075").ifPresent(product -> {
			product.setStock(2);
			repoProduct.save(product);
		});

		mockMvc.perform(post("/invoice").header("Authorization", CUSTOMER_TOKEN))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Stock insuficiente para el producto Coca-cola 600 ml")));
	}

	@Test
	void requestsWithoutTokenAreRejected() throws Exception {
		mockMvc.perform(get("/cart-item"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void checkoutPersistsShippingPaymentAndCoupon() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);
		addCartItem("7501055300075", 2);

		// Solo el administrador puede crear cupones
		mockMvc.perform(post("/coupon")
				.header("Authorization", CUSTOMER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"PROMO10\",\"discount\":10}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/coupon")
				.header("Authorization", ADMIN_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"PROMO10\",\"discount\":10}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code", is("PROMO10")));

		mockMvc.perform(post("/invoice")
				.header("Authorization", CUSTOMER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "shippingAddress": "Av. Universidad 3000, CDMX, 04510",
						  "paymentMethod": "TARJETA",
						  "cardLast4": "1234",
						  "couponCode": "PROMO10"
						}
						"""))
				.andExpect(status().isOk());

		// Total 42.00 - 10% = 37.80; impuestos 6.05 (16%); subtotal 31.75
		Invoice invoice = repoInvoice.findAll().get(0);
		org.assertj.core.api.Assertions.assertThat(invoice.getDiscount()).isEqualByComparingTo(new BigDecimal("4.20"));
		org.assertj.core.api.Assertions.assertThat(invoice.getTotal()).isEqualByComparingTo(new BigDecimal("37.80"));
		org.assertj.core.api.Assertions.assertThat(invoice.getTaxes()).isEqualByComparingTo(new BigDecimal("6.05"));
		org.assertj.core.api.Assertions.assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("31.75"));
		org.assertj.core.api.Assertions.assertThat(invoice.getCoupon_code()).isEqualTo("PROMO10");
		org.assertj.core.api.Assertions.assertThat(invoice.getShipping_address()).isEqualTo("Av. Universidad 3000, CDMX, 04510");
		org.assertj.core.api.Assertions.assertThat(invoice.getPayment_method()).isEqualTo("TARJETA");
		org.assertj.core.api.Assertions.assertThat(invoice.getCard_last4()).isEqualTo("1234");
	}

	@Test
	void checkoutRejectsUnknownCoupon() throws Exception {
		createProduct("7501055300075", "Coca-cola 600 ml", "21.00", 100);
		addCartItem("7501055300075", 2);

		mockMvc.perform(post("/invoice")
				.header("Authorization", CUSTOMER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"couponCode\":\"NOEXISTE\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is("El cupón no existe")));
	}

	private void createProduct(String gtin, String name, String price, int stock) throws Exception {
		mockMvc.perform(post("/product")
				.header("Authorization", ADMIN_TOKEN)
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
				.header("Authorization", CUSTOMER_TOKEN)
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
