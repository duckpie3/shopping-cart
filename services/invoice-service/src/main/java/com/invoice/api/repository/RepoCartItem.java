package com.invoice.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.invoice.api.entity.CartItem;

@Repository
public interface RepoCartItem extends JpaRepository<CartItem, Integer> {

	@Query("SELECT c FROM CartItem c WHERE c.user_id = :user_id AND c.status = true")
	List<CartItem> findAllByUserIdAndStatusTrue(@Param("user_id") Integer user_id);

	@Query("SELECT c FROM CartItem c WHERE c.cart_item_id = :cart_item_id AND c.user_id = :user_id AND c.status = true")
	Optional<CartItem> findByCartItemIdAndUserIdAndStatusTrue(
			@Param("cart_item_id") Integer cart_item_id,
			@Param("user_id") Integer user_id);

	@Query("SELECT c FROM CartItem c WHERE c.user_id = :user_id AND c.gtin = :gtin AND c.status = true")
	Optional<CartItem> findByUserIdAndGtinAndStatusTrue(@Param("user_id") Integer user_id, @Param("gtin") String gtin);
}
