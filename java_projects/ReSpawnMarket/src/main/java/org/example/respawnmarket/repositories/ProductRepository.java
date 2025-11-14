package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer>
{
}
