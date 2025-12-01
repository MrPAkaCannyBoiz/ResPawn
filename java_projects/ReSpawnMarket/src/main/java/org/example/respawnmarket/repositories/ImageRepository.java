package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageEntity, Integer>
{
    @Query("""
            select i from ImageEntity i
                     where i.product.id = :productId
            """)
    List<ImageEntity> findAllByProductId(@Param("productId") int productId);
}
