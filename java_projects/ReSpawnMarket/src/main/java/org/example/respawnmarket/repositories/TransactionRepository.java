package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer>
{
}
