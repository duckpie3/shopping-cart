package com.invoice.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.invoice.api.entity.Product;

@Repository
public interface RepoProduct extends JpaRepository<Product, String> {

	List<Product> findAllByStatusTrue();
	Optional<Product> findByGtinAndStatusTrue(String gtin);
}
