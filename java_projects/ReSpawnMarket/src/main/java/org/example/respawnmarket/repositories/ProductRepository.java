package org.example.respawnmarket.repositories;

import java.util.List;

import org.example.respawnmarket.dtos.ProductInspectionDTO;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
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
        SELECT new org.example.respawnmarket.dtos.ProductInspectionDTO(p, i.comment)
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

