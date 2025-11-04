package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.AddressEntity;
import org.example.respawnmarket.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer>
{
    // custom query to find address by customer id
    @Query("""
            select ca.address from CustomerAddressEntity ca
            where ca.customer.id = :customerId
            """)
    List<AddressEntity> findAddressByCustomerId(@Param("customerId") Integer customerId);
}
