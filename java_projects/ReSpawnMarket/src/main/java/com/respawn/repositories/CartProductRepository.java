package com.respawn.repositories;

import com.respawn.entities.CartProductEntity;
import com.respawn.entities.CartProductId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartProductRepository extends JpaRepository<CartProductEntity, CartProductId>
{
}
