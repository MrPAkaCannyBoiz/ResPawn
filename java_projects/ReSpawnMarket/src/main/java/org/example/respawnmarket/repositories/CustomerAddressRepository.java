package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.CustomerAddressEntity;
import org.example.respawnmarket.entities.CustomerAddressIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddressEntity, CustomerAddressIdEntity>
{

}
