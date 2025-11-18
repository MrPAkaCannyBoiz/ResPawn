package org.example.respawnmarket.Service;


import java.time.Instant;
import java.util.List;

import org.example.respawnmarket.dtos.ProductInspectionDto;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.ApprovalStatus;
import com.respawnmarket.Category;
import com.respawnmarket.GetPendingProductsRequest;
import com.respawnmarket.GetPendingProductsResponse;
import com.respawnmarket.Product;
import com.respawnmarket.ProductInspectionRequest;
import com.respawnmarket.ProductInspectionResponse;
import com.respawnmarket.ProductInspectionServiceGrpc;

import io.grpc.stub.StreamObserver;

@Service
public class ProductInspectionGrpcService extends ProductInspectionServiceGrpc.ProductInspectionServiceImplBase
{
    private final ProductInspectionServiceImpl productInspectionService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductInspectionGrpcService(ProductInspectionServiceImpl productInspectionService,
                                        ProductRepository productRepository)
    {
        this.productInspectionService = productInspectionService;
        this.productRepository = productRepository;
    }

    @Override
    public void getPendingProducts(GetPendingProductsRequest request,
                                   StreamObserver<GetPendingProductsResponse> responseObserver)
    {
        List<ProductEntity> entities = productInspectionService.getPendingProducts();

        GetPendingProductsResponse.Builder responseBuilder = GetPendingProductsResponse.newBuilder();
        for (ProductEntity entity : entities)
        {
            Product productProto = toProtoProduct(entity);
            responseBuilder.addProducts(productProto);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void reviewProduct(ProductInspectionRequest request,
                              StreamObserver<ProductInspectionResponse> responseObserver)
    {
        ProductInspectionDto dto = new ProductInspectionDto(
                request.getProductId(),
                request.getResellerId(),
                request.getStatus(),
                request.getComment()
        );

        ProductEntity updated = productInspectionService.reviewProduct(dto);

        ProductInspectionResponse response;
        if (updated == null)
        {
            response = ProductInspectionResponse.newBuilder()
                    .setProductId(request.getProductId())
                    .setStatus("")
                    .setComment("")
                    .build();
        }
        else
        {
            String status = updated.getApprovalStatus() != null
                    ? updated.getApprovalStatus().name()
                    : "";
            String comment = updated.getResellerComment() != null
                    ? updated.getResellerComment()
                    : "";

            response = ProductInspectionResponse.newBuilder()
                    .setProductId(updated.getId())
                    .setStatus(status)
                    .setComment(comment)
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Product toProtoProduct(ProductEntity entity)
    {
        ApprovalStatus approvalStatus = toProtoApprovalStatus(entity.getApprovalStatus());
        Category category = toProtoCategory(entity.getCategory());

        Instant instant = entity.getRegisterDate().toInstant(java.time.ZoneOffset.UTC);
        Timestamp registerDateTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(0)
                .build();

        return Product.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setPrice(entity.getPrice())
                .setCondition(entity.getCondition())
                .setDescription(entity.getDescription())
                .setPhotoUrl(entity.getPhotoUrl() == null ? "" : entity.getPhotoUrl())
                .setSoldByCustomerId(entity.getSeller().getId())
                .setCategory(category)
                .setSold(entity.isSold())
                .setApprovalStatus(approvalStatus)
                .setRegisterDate(registerDateTimestamp)
                .build();
    }

    private ApprovalStatus toProtoApprovalStatus(ApprovalStatusEnum entityApprovalStatus)
    {
        if (entityApprovalStatus == null)
        {
            return ApprovalStatus.PENDING;
        }

        return switch (entityApprovalStatus)
        {
            case PENDING -> ApprovalStatus.PENDING;
            case APPROVED -> ApprovalStatus.APPROVED;
            case NOT_APPROVED, REJECTED -> ApprovalStatus.NOT_APPROVED;
        };
    }

    private Category toProtoCategory(CategoryEnum entityCategory)
    {
        if (entityCategory == null)
        {
            return Category.CATEGORY_UNSPECIFIED;
        }

        return switch (entityCategory)
        {
            case ELECTRONICS -> Category.ELECTRONICS;
            case JEWELRY -> Category.JEWELRY;
            case WATCHES -> Category.WATCHES;
            case MUSICAL_INSTRUMENTS -> Category.MUSICAL_INSTRUMENTS;
            case TOOLS -> Category.TOOLS;
            case VEHICLES -> Category.VEHICLES;
            case COLLECTIBLES -> Category.COLLECTIBLES;
            case FASHION -> Category.FASHION;
            case HOME_APPLIANCES -> Category.HOME_APPLIANCES;
            case SPORTS_EQUIPMENT -> Category.SPORTS_EQUIPMENT;
            case COMPUTERS -> Category.COMPUTERS;
            case MOBILE_PHONES -> Category.MOBILE_PHONES;
            case CAMERAS -> Category.CAMERAS;
            case LUXURY_ITEMS -> Category.LUXURY_ITEMS;
            case ARTWORK -> Category.ARTWORK;
            case ANTIQUES -> Category.ANTIQUES;
            case GAMING_CONSOLES -> Category.GAMING_CONSOLES;
            case FURNITURE -> Category.FURNITURE;
            case GOLD_AND_SILVER -> Category.GOLD_AND_SILVER;
            case OTHER -> Category.OTHER;
        };
    }
}
