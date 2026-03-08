package com.respawn.repositories;

import com.respawn.entities.CustomerAddressEntity;
import com.respawn.entities.CustomerAddressId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddressEntity, CustomerAddressId>
{

}
