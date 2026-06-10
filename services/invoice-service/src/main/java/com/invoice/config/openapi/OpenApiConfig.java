package com.invoice.config.openapi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME = "bearerAuth";

	@Bean
   public OpenAPI customOpenAPI() {
       		return new OpenAPI()
           			.info(new Info()
             .title("DWB - API Customer")
             .version("0.0.1")
             .description("API para la gestión clientes para la tienda en línea FCiencias Store."))
       			.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
       			.components(new Components().addSecuritySchemes(SECURITY_SCHEME,
       					new SecurityScheme()
       							.type(SecurityScheme.Type.HTTP)
       							.scheme("bearer")
       							.bearerFormat("JWT")));
   	}

	@Bean
	public OpenApiCustomizer sortSchemasAlphabetically() {
	   return openApi -> {
	       Components components = openApi.getComponents();
	       if (components != null && components.getSchemas() != null) {
	           Map<String, Schema> sortedSchemas = components.getSchemas().entrySet().stream()
	                   .sorted(Map.Entry.comparingByKey())
	                   .collect(Collectors.toMap(
	                       Map.Entry::getKey,
	                       Map.Entry::getValue,
	                       (oldValue, newValue) -> oldValue,
	                       LinkedHashMap::new
	                   ));
	           components.setSchemas(sortedSchemas);
	       }
	   };
	}

}

