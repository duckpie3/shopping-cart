package com.invoice.commons.util;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.invoice.exception.ApiException;

/**
 * Identidad del usuario obtenida del token JWT (cargada por JwtAuthFilter).
 */
@Component
public class UserContext {

	public Integer getUserId() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			@SuppressWarnings("unchecked")
			Map<String, Object> payload = (Map<String, Object>) authentication.getCredentials();
			return (Integer) payload.get("id");
		} catch (Exception e) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "El usuario es inválido");
		}
	}

	public boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities()
				.stream()
				.anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
	}
}
