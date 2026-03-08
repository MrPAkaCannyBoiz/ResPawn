package com.respawn.repositories;

import com.respawn.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer>
{
  CustomerEntity findByEmail(String email);
}
