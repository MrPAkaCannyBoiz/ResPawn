package com.respawn.repositories;

import java.util.List;

import com.respawn.dtos.ProductInspectionDTO;
import com.respawn.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer>
{
    @Query("""
            select p from ProductEntity p
                     where p.approvalStatus = "PENDING"
                     order by p.id
            """)
    List<ProductEntity> findPendingProduct();

    @Query("""
            select p from ProductEntity p
                     where (upper(p.approvalStatus) = "APPROVED")
                     and p.sold = false
                     order by p.id
            """)
    List<ProductEntity> findAllAvailableProducts();

    @Query("""
            select p from ProductEntity p
                     where (upper(p.approvalStatus) = "REVIEWING")
                     order by p.id
            """)
    List<ProductEntity> findAllReviewingProducts();

  @Query("""
        SELECT new com.respawn.dtos.ProductInspectionDTO(p, i.comment)
        FROM ProductEntity p
        LEFT JOIN InspectionEntity i
          ON i.product = p
          AND i.inspectionDate = (
              SELECT MAX(i2.inspectionDate)
              FROM InspectionEntity i2
              WHERE i2.product = p
          )
        WHERE p.seller.id = :customerId
          AND ((upper(p.approvalStatus) = "PENDING")
              OR (upper(p.approvalStatus) = "REVIEWING"))
        ORDER BY p.id
    """)
  List<ProductInspectionDTO> findProductsWithLatestInspection(
      @Param("customerId") int customerId
  );


}

