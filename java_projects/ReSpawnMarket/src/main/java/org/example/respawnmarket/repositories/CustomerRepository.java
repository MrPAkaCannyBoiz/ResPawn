package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer>
{
  boolean existsByEmail(String email);
  CustomerEntity findByEmail(String email);
}
