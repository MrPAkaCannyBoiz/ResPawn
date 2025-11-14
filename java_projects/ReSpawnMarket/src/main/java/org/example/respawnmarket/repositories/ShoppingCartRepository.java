package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.ShoppingCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, Integer>
{
}
