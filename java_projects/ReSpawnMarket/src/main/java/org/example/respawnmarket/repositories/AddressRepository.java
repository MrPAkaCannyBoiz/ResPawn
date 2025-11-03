package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer>
{
}
