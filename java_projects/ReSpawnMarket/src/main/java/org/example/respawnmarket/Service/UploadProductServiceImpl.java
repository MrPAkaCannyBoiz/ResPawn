package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.dtos.ProductDto;
import org.example.respawnmarket.entities.CustomerEntity;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.example.respawnmarket.entities.enums.CategoryEnum.*;

@Service
public class UploadProductServiceImpl extends com.respawnmarket.UploadProductServiceGrpc.UploadProductServiceImplBase
{
    private final JpaVendorAdapter jpaVendorAdapter;
    // upload product involve product, stock, customer
    private ProductRepository productRepository;
    private StockRepository stockRepository;
    private CustomerRepository customerRepository;

    @Autowired
    public UploadProductServiceImpl(ProductRepository productRepository,
                                    StockRepository stockRepository,
                                    CustomerRepository customerRepository, JpaVendorAdapter jpaVendorAdapter)
    {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.customerRepository = customerRepository;
        this.jpaVendorAdapter = jpaVendorAdapter;
    }

    public void uploadProduct(com.respawnmarket.UploadProductRequest request
            , StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver)
    {
        CustomerEntity givenCustomer = customerRepository
                .findById(request.getSoldByCustomerId()).orElse(null);
        assert givenCustomer != null;
        var product = new ProductEntity(request.getName(),
                request.getPrice(),
                request.getCondition(),
                request.getDescription(),
                request.getPhotoUrl(),
                givenCustomer,
                toEntityCategory(request.getCategory()));
        ProductEntity newProduct = productRepository.save(product);

        Instant instant = newProduct.getRegisterDate().toInstant(java.time.ZoneOffset.UTC);
        Timestamp registerDateTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(0)
                .build();

        com.respawnmarket.Product productDto = com.respawnmarket.Product.newBuilder().setId(newProduct.getId())
                .setName(newProduct.getName())
                .setPrice(newProduct.getPrice())
                .setCondition(newProduct.getCondition())
                .setDescription(newProduct.getDescription())
                .setPhotoUrl(newProduct.getPhotoUrl())
                .setSoldByCustomerId(newProduct.getSeller().getId())
                .setCategory(request.getCategory())
                .setSold(newProduct.isSold())
                .setApprovalStatus(toProtoApprovalStatus(newProduct.getApprovalStatus()))
                .setRegisterDate(registerDateTimestamp)
                .build();
        com.respawnmarket.UploadProductResponse response = com.respawnmarket.UploadProductResponse.newBuilder()
                .setProduct(productDto)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    // convert proto category to entity category to match ProductEntity constructor parameter
    private CategoryEnum toEntityCategory(com.respawnmarket.Category protoCategory)
    {
        return switch (protoCategory) {
            case CATEGORY_UNSPECIFIED -> null;
            case ELECTRONICS -> ELECTRONICS;
            case JEWELRY ->  JEWELRY;
            case WATCHES ->  WATCHES;
            case MUSICAL_INSTRUMENTS -> MUSICAL_INSTRUMENTS;
            case TOOLS -> TOOLS;
            case VEHICLES -> VEHICLES;
            case COLLECTIBLES -> COLLECTIBLES;
            case FASHION -> FASHION;
            case HOME_APPLIANCES -> HOME_APPLIANCES;
            case SPORTS_EQUIPMENT -> SPORTS_EQUIPMENT;
            case COMPUTERS -> COMPUTERS;
            case MOBILE_PHONES -> MOBILE_PHONES;
            case CAMERAS -> CAMERAS;
            case LUXURY_ITEMS -> LUXURY_ITEMS;
            case ARTWORK -> ARTWORK;
            case ANTIQUES -> ANTIQUES;
            case GAMING_CONSOLES -> GAMING_CONSOLES;
            case FURNITURE -> FURNITURE;
            case GOLD_AND_SILVER -> GOLD_AND_SILVER;
            case OTHER -> OTHER;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unrecognized category: " + protoCategory);
        };
    }

    private com.respawnmarket.ApprovalStatus toProtoApprovalStatus(ApprovalStatusEnum entityApprovalStatus)
    {
        return switch (entityApprovalStatus) {
            case PENDING -> com.respawnmarket.ApprovalStatus.PENDING;
            case APPROVED -> com.respawnmarket.ApprovalStatus.APPROVED;
            case NOT_APPROVED -> com.respawnmarket.ApprovalStatus.NOT_APPROVED;
            case REJECTED -> com.respawnmarket.ApprovalStatus.REJECTED;
        };
    }
}
