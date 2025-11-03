package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer>
{
}
