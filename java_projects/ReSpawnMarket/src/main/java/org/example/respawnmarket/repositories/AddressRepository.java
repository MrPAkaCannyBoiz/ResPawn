package org.example.respawnmarket.repositories;

import org.example.respawnmarket.dtos.PawnshopAddressPostalDto;
import org.example.respawnmarket.entities.AddressEntity;
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

    @Query("""
            select new org.example.respawnmarket.dtos.PawnshopAddressPostalDto(
                a.id, a.streetName,a.secondaryUnit, p.postalCode, p.city, ps.id)
            from PawnshopEntity ps
            join AddressEntity a on ps.address.id = a.id
            join PostalEntity p on a.postal.postalCode = p.postalCode
           """)
    List<PawnshopAddressPostalDto> getAllPawnshopAddresses();


}

