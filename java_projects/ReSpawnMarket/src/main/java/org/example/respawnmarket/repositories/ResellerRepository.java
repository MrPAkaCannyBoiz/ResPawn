package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.ResellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResellerRepository extends JpaRepository<ResellerEntity, Integer>
{
    ResellerEntity findByUsername(String username);
}
