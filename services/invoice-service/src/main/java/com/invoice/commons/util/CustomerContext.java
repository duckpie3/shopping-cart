package com.invoice.commons.util;

import org.springframework.stereotype.Component;

@Component
public class CustomerContext {

	private final JwtDecoder jwtDecoder;

	public CustomerContext(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	public Integer getCustomerId() {
		return jwtDecoder.getUserId();
	}
}
