package org.example.respawnmarket.Service;

import java.time.Instant;
import java.util.List;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.transaction.Transactional;
import org.example.respawnmarket.dtos.ProductInspectionDTO;
import org.example.respawnmarket.entities.*;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.repositories.InspectionRepository;
import org.example.respawnmarket.repositories.PawnshopRepository;
import org.example.respawnmarket.repositories.ProductRepository;
import org.example.respawnmarket.repositories.ResellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import io.grpc.stub.StreamObserver;

import static org.example.respawnmarket.Service.ServiceExtensions.ApprovalStatusExtension.toProtoApprovalStatus;
import static org.example.respawnmarket.Service.ServiceExtensions.CategoryExtension.toProtoCategory;

@Service
public class ProductInspectionServiceImpl extends com.respawnmarket.ProductInspectionServiceGrpc.ProductInspectionServiceImplBase
{
    private ProductRepository productRepository;
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
    @Transactional
    public void reviewProduct(com.respawnmarket.ProductInspectionRequest request,
                              StreamObserver<com.respawnmarket.ProductInspectionResponse> responseObserver)
    {
      try
      {
        int productId  = request.getProductId();
        int resellerId = request.getResellerId();
        int pawnshopId = request.getPawnshopId();

        // Load product
        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Product not found: " + productId)
                .asRuntimeException());

        //  Load reseller
        ResellerEntity reseller = resellerRepository.findById(resellerId)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Reseller not found: " + resellerId)
                .asRuntimeException());


        PawnshopEntity pawnshop = null;
        if (request.getIsAccepted())
        {
          pawnshop = pawnshopRepository.findById(pawnshopId)
              .orElseThrow(() -> Status.NOT_FOUND
                  .withDescription("Pawnshop not found: " + pawnshopId)
                  .asRuntimeException());
        }

        //  Save inspection
        InspectionEntity inspection = new InspectionEntity(
            product,
            reseller,
            request.getComments(),
            request.getIsAccepted()
        );
        inspectionRepository.save(inspection);

        // Update product
        if (request.getIsAccepted())
        {
          product.setApprovalStatus(ApprovalStatusEnum.REVIEWING);
          product.setPawnshop(pawnshop);
        }
        else if (!request.getIsAccepted() && !request.getComments().isEmpty())
        {
          product.setApprovalStatus(ApprovalStatusEnum.REJECTED);
          product.setPawnshop(null); // rejected => no pawnshop
        }
        else
        {
            throw Status.INVALID_ARGUMENT
                .withDescription("Comments must be provided when rejecting a product")
                .asRuntimeException();
        }

        productRepository.save(product);

        //  response
        com.respawnmarket.ProductInspectionResponse response = com.respawnmarket.ProductInspectionResponse.newBuilder()
            .setProductId(product.getId())
            .setApprovalStatus(toProtoApprovalStatus(product.getApprovalStatus()))
            .setPawnshopId(product.getPawnshop() != null
                ? product.getPawnshop().getId()
                : 0)
            .setComments(inspection.getComment())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
      catch (StatusRuntimeException e)
      {
        responseObserver.onError(e);
      }
      catch (Exception e)
      {
        responseObserver.onError(
            Status.INTERNAL
                .withDescription("Failed to review product")
                .withCause(e)
                .asRuntimeException()
        );
      }
    }

    @Override
    @Transactional
    public void verifyProduct(com.respawnmarket.ProductVerificationRequest request,
                              StreamObserver<com.respawnmarket.ProductVerificationResponse> responseObserver)
    {
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> Status
                .NOT_FOUND
                .withDescription(
                        "Product not found/ The Product is not in 'REVIEWING' status")
                .asRuntimeException());
        ResellerEntity resellerWhoChecks = resellerRepository.
                findById(request.getResellerId()).orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Reseller not found, id:" + request.getResellerId())
                        .asRuntimeException());
        InspectionEntity inspection = new InspectionEntity
                (product, resellerWhoChecks, request.getComments(), request.getIsAccepted());
        inspection.setApprovalStage(ApprovalStatusEnum.APPROVED);
        inspectionRepository.save(inspection);
        assert product != null;
        if (request.getIsAccepted()) //true -> approved
        {
            product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
        }
        else if (!request.getIsAccepted() && !request.getComments().isEmpty())
        {
            product.setApprovalStatus(ApprovalStatusEnum.REJECTED);
            product.setPawnshop(null); // rejected => no pawnshop
        }
        else
        {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Comments must be provided when rejecting a product")
                    .asRuntimeException();
        }
        productRepository.save(product); // update product status
        productRepository.flush();

        com.respawnmarket.ProductVerificationResponse response = com.respawnmarket.ProductVerificationResponse.newBuilder()
                .setProductId(product.getId())
                .setApprovalStatus(toProtoApprovalStatus(product.getApprovalStatus()))
                .setComments(inspection.getComment())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }



}



