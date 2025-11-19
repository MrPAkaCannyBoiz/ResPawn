package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.*;
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

@Service
public class UploadProductServiceImpl extends UploadProductServiceGrpc.UploadProductServiceImplBase
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

    public void uploadProduct(UploadProductRequest request
            , StreamObserver<UploadProductResponse> responseObserver)
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

        Product productDto = Product.newBuilder().setId(newProduct.getId())
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
        UploadProductResponse response = UploadProductResponse.newBuilder()
                .setProduct(productDto)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // convert proto category to entity category to match ProductEntity constructor parameter
    private CategoryEnum toEntityCategory(Category protoCategory)
    {
        return switch (protoCategory) {
            case CATEGORY_UNSPECIFIED -> null;
            case ELECTRONICS -> CategoryEnum.ELECTRONICS;
            case JEWELRY ->  CategoryEnum.JEWELRY;
            case WATCHES ->  CategoryEnum.WATCHES;
            case MUSICAL_INSTRUMENTS -> CategoryEnum.MUSICAL_INSTRUMENTS;
            case TOOLS -> CategoryEnum.TOOLS;
            case VEHICLES -> CategoryEnum.VEHICLES;
            case COLLECTIBLES -> CategoryEnum.COLLECTIBLES;
            case FASHION -> CategoryEnum.FASHION;
            case HOME_APPLIANCES -> CategoryEnum.HOME_APPLIANCES;
            case SPORTS_EQUIPMENT -> CategoryEnum.SPORTS_EQUIPMENT;
            case COMPUTERS -> CategoryEnum.COMPUTERS;
            case MOBILE_PHONES -> CategoryEnum.MOBILE_PHONES;
            case CAMERAS -> CategoryEnum.CAMERAS;
            case LUXURY_ITEMS -> CategoryEnum.LUXURY_ITEMS;
            case ARTWORK -> CategoryEnum.ARTWORK;
            case ANTIQUES -> CategoryEnum.ANTIQUES;
            case GAMING_CONSOLES -> CategoryEnum.GAMING_CONSOLES;
            case FURNITURE -> CategoryEnum.FURNITURE;
            case GOLD_AND_SILVER -> CategoryEnum.GOLD_AND_SILVER;
            case OTHER -> CategoryEnum.OTHER;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unrecognized category: " + protoCategory);
        };
    }

    private ApprovalStatus toProtoApprovalStatus(ApprovalStatusEnum entityApprovalStatus)
    {
        return switch (entityApprovalStatus) {
            case PENDING -> ApprovalStatus.PENDING;
            case APPROVED -> ApprovalStatus.APPROVED;
            case NOT_APPROVED -> ApprovalStatus.NOT_APPROVED;
            case REJECTED -> ApprovalStatus.REJECTED;
        };
    }
}
