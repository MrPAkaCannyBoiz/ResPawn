package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.CustomerAddressEntity;
import org.example.respawnmarket.entities.CustomerAddressId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddressEntity, CustomerAddressId>
{

}
