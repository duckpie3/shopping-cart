package com.invoice.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler{

	private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	protected ResponseEntity<ExceptionResponse> handleApiException(ApiException exception, WebRequest request){
		
		ExceptionResponse response = new ExceptionResponse();
		response.setTimestamp(LocalDateTime.now());
		response.setStatus(exception.getStatus().value());
		response.setError(exception.getStatus());
		response.setMessage(exception.getMessage());
		response.setPath(((ServletWebRequest)request).getRequest().getRequestURI().toString());
		
		return new ResponseEntity<>(response, response.getError());

	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid( MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
	    
		log.debug("Validación fallida: {}", ex.getLocalizedMessage());

		ExceptionResponse response = new ExceptionResponse();

	    String message = ex.getBindingResult().getFieldError() != null
	    		? ex.getBindingResult().getFieldError().getDefaultMessage()
	    		: "Datos de la petición inválidos";

	    response.setTimestamp(LocalDateTime.now());
	    response.setStatus(HttpStatus.BAD_REQUEST.value());
	    response.setError(HttpStatus.BAD_REQUEST);
	    response.setMessage(message);
	    response.setPath(((ServletWebRequest)request).getRequest().getRequestURI().toString());


	    return new ResponseEntity<>(response, response.getError());
	}


}
