package com.invoice.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.invoice.api.entity.Coupon;

@Repository
public interface RepoCoupon extends JpaRepository<Coupon, Integer> {

	@Query("SELECT c FROM Coupon c WHERE c.status = true")
	List<Coupon> findAllByStatusTrue();

	@Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.status = true")
	Optional<Coupon> findByCodeAndStatusTrue(@Param("code") String code);

	@Query("SELECT COUNT(c) > 0 FROM Coupon c WHERE c.code = :code")
	boolean existsByCode(@Param("code") String code);
}
