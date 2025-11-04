package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.PostalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostalRepository
        extends JpaRepository<PostalEntity, Integer>
{
    @Query("""
            select p from PostalEntity p
            join AddressEntity a on p.postalCode = a.postal.postalCode
            join CustomerAddressEntity ca on ca.address.id = a.id
            where ca.customer.id = :customerId
            """)
    List<PostalEntity> findByCustomerId(@Param("customerId") int customerId);
}
