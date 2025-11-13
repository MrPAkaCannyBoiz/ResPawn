package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.CartProductEntity;
import org.example.respawnmarket.entities.CartProductId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartProductRepository extends JpaRepository<CartProductEntity, CartProductId>
{
}
