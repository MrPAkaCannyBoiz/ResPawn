package org.example.respawnmarket.Service;

import java.util.List;

import org.example.respawnmarket.dtos.ProductInspectionDto;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductInspectionServiceImpl {
        private final ProductRepository productRepository;

    @Autowired
    public ProductInspectionServiceImpl(ProductRepository productRepository)
    {
        this.productRepository = productRepository;
    }

    public List<ProductEntity> getPendingProducts()
    {
        return productRepository.findByApprovalStatus("PENDING");
    }

    public ProductEntity reviewProduct(ProductInspectionDto dto)
    {
        ProductEntity product = productRepository.findById(dto.getProductId()).orElse(null);
        if (product == null) return null;

        if (dto.getStatus().equals("REJECTED") &&
                (dto.getComment() == null || dto.getComment().isBlank()))
            throw new RuntimeException("Please write a reason for the denial of purchase");

        product.setApprovalStatus(ApprovalStatusEnum.valueOf(dto.getStatus()));
        product.setResellerComment(dto.getComment());
        product.setInspectedByResellerId(dto.getResellerId());

        return productRepository.save(product);
    }
}
