package org.example.respawnmarket.Service;

import java.time.Instant;
import java.util.List;

import org.example.respawnmarket.dtos.ProductInspectionDto;
import org.example.respawnmarket.entities.InspectionEntity;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.ResellerEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.InspectionRepository;
import org.example.respawnmarket.repositories.ProductRepository;
import org.example.respawnmarket.repositories.ResellerRepository;
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
public class ProductInspectionServiceImpl extends ProductInspectionServiceGrpc.ProductInspectionServiceImplBase
{
    private final ProductRepository productRepository;
    private InspectionRepository inspectionRepository;
    private ResellerRepository resellerRepository;

    @Autowired
    public ProductInspectionServiceImpl(ProductRepository productRepository
            , InspectionRepository inspectionRepository,  ResellerRepository resellerRepository)
    {
        this.productRepository = productRepository;
        this.inspectionRepository = inspectionRepository;
        this.resellerRepository = resellerRepository;
    }

    @Override
    public void getPendingProducts(GetPendingProductsRequest request,
                                   StreamObserver<GetPendingProductsResponse> responseObserver)
    {
        List<ProductEntity> entities = productRepository.findByApprovalStatus(ApprovalStatusEnum.PENDING);

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
        ProductEntity pendingProduct = productRepository.findPendingProduct(request.getProductId());
        ResellerEntity resellerWhoChecks = resellerRepository.
                findById(request.getResellerId()).orElse(null);
        assert resellerWhoChecks != null;
        assert pendingProduct != null;

        var inspection = new InspectionEntity
                (pendingProduct, resellerWhoChecks ,request.getResult(), request.getComments() ,request.getIsAccepted());
        if (request.getIsAccepted())
        {
            pendingProduct.setApprovalStatus(ApprovalStatusEnum.APPROVED);
            productRepository.save(pendingProduct);
        }
        else
        {
            pendingProduct.setApprovalStatus(ApprovalStatusEnum.REJECTED);
            productRepository.save(pendingProduct);
        }
        inspectionRepository.save(inspection);



        ProductInspectionResponse response = ProductInspectionResponse.newBuilder()
                .setProductId(pendingProduct.getId())
                .setApprovalStatus(toProtoApprovalStatus(pendingProduct.getApprovalStatus()))
                .build();

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

