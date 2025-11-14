package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.StockEntity;
import org.example.respawnmarket.entities.StockId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockEntity, StockId>
{
}
