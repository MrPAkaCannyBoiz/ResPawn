package org.example.respawnmarket.repositories;

import java.util.List;

import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer>
{
    List<ProductEntity> findByApprovalStatus(ApprovalStatusEnum approvalStatus);

    @Query("""
            select p from ProductEntity p
                     where p.approvalStatus = "PENDING"
                                 and p.id = :productId
            """)
    ProductEntity findPendingProduct(@Param("productId") Integer productId);
}

