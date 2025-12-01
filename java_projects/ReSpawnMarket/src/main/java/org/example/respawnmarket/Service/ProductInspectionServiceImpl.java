package org.example.respawnmarket.Service;

import java.time.Instant;
import java.util.List;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.example.respawnmarket.entities.InspectionEntity;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.ResellerEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.InspectionRepository;
import org.example.respawnmarket.repositories.PawnshopRepository;
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
    private PawnshopRepository pawnshopRepository;

    @Autowired
    public ProductInspectionServiceImpl(ProductRepository productRepository
            , InspectionRepository inspectionRepository
            ,  ResellerRepository resellerRepository, PawnshopRepository pawnshopRepository)
    {
        this.productRepository = productRepository;
        this.inspectionRepository = inspectionRepository;
        this.resellerRepository = resellerRepository;
        this.pawnshopRepository = pawnshopRepository;
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
      try
      {
        int productId  = request.getProductId();
        int resellerId = request.getResellerId();
        int pawnshopId = request.getPawnshopId();

        // 1) Load product
        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Product not found: " + productId)
                .asRuntimeException());

        // 2) Load reseller
        ResellerEntity resellerWhoChecks = resellerRepository.findById(resellerId)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Reseller not found: " + resellerId)
                .asRuntimeException());

        // 3) Only load pawnshop if APPROVED
        //    (for rejected products we keep pawnshop = null)
        var pawnshop = request.getIsAccepted()
            ? pawnshopRepository.findById(pawnshopId)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Pawnshop not found: " + pawnshopId)
                .asRuntimeException())
            : null;

        // 4) Save inspection row
        InspectionEntity inspection = new InspectionEntity(
            product,
            resellerWhoChecks,
            request.getComments(),
            request.getIsAccepted()
        );
        inspectionRepository.save(inspection);

        // 5) Update product state
        product.setApprovalStatus(
            request.getIsAccepted()
                ? ApprovalStatusEnum.APPROVED
                : ApprovalStatusEnum.REJECTED
        );

        if (request.getIsAccepted())
        {
          product.setPawnshop(pawnshop);
        }
        else
        {
          product.setPawnshop(null); // no pawnshop for rejected products
        }

        productRepository.save(product);

        // 6) Build response safely (handle null pawnshop) omg this null execptions are crazy
        ProductInspectionResponse response = ProductInspectionResponse.newBuilder()
            .setProductId(product.getId())
            .setApprovalStatus(toProtoApprovalStatus(product.getApprovalStatus()))
            .setPawnshopId(product.getPawnshop() != null
                ? product.getPawnshop().getId()
                : 0) // 0 = "no pawnshop"
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
      catch (StatusRuntimeException e)
      {
        // controlled gRPC errors (NOT_FOUND, etc.)
        responseObserver.onError(e);
      }
      catch (Exception e)
      {
        // unexpected errors -> INTERNAL with message
        e.printStackTrace();
        responseObserver.onError(
            Status.INTERNAL
                .withDescription("Internal error during review")
                .withCause(e)
                .asRuntimeException());
      }
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

