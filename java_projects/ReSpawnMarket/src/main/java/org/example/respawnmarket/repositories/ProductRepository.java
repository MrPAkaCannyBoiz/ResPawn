package org.example.respawnmarket.repositories;

import java.util.List;

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


}

