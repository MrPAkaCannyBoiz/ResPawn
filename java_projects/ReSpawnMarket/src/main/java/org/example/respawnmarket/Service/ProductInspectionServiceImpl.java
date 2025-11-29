package org.example.respawnmarket.Service;

import java.time.Instant;
import java.util.List;

import io.grpc.Status;
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
    public void reviewProduct(ProductInspectionRequest request,
                              StreamObserver<ProductInspectionResponse> responseObserver)
    {
        ProductEntity product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null)
        {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("Product not found").asRuntimeException());
            return;
        }
        ResellerEntity resellerWhoChecks = resellerRepository.
                findById(request.getResellerId()).orElse(null);
        if (resellerWhoChecks == null)
        {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("Reseller not found").asRuntimeException());
            return;
        }

        InspectionEntity inspection = new InspectionEntity
                (product, resellerWhoChecks, request.getComments(), request.getIsAccepted());
        inspectionRepository.save(inspection);
        if (request.getIsAccepted()) //true -> approved
        {
            product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
            product.setPawnshop(pawnshopRepository.findById(request.getPawnshopId()).orElse(null));
            productRepository.save(product);
        }
        else // false -> rejected
        {
            product.setApprovalStatus(ApprovalStatusEnum.REJECTED);
            productRepository.save(product);
        }

        ProductInspectionResponse response = ProductInspectionResponse.newBuilder()
                .setProductId(product.getId())
                .setApprovalStatus(toProtoApprovalStatus(product.getApprovalStatus()))
                .setPawnshopId(product.getPawnshop().getId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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



}

