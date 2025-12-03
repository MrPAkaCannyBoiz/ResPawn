package org.example.respawnmarket.Service;

import java.time.Instant;
import java.util.List;

import com.respawnmarket.*;
import com.respawnmarket.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.transaction.Transactional;
import org.example.respawnmarket.entities.InspectionEntity;
import org.example.respawnmarket.entities.PawnshopEntity;
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

import io.grpc.stub.StreamObserver;

import static org.example.respawnmarket.Service.ServiceExtensions.ApprovalStatusExtension.toProtoApprovalStatus;

@Service
public class ProductInspectionServiceImpl extends ProductInspectionServiceGrpc.ProductInspectionServiceImplBase
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
    public void reviewProduct(ProductInspectionRequest request,
                              StreamObserver<ProductInspectionResponse> responseObserver)
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
          product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
          product.setPawnshop(pawnshop);
        }
        else
        {
          product.setApprovalStatus(ApprovalStatusEnum.REJECTED);
          product.setPawnshop(null); // rejected => no pawnshop
        }

        productRepository.save(product);

        //  response
        ProductInspectionResponse response = ProductInspectionResponse.newBuilder()
            .setProductId(product.getId())
            .setApprovalStatus(toProtoApprovalStatus(product.getApprovalStatus()))
            .setPawnshopId(product.getPawnshop() != null
                ? product.getPawnshop().getId()
                : 0)
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
    public void verifyProduct(ProductVerificationRequest request,
                              StreamObserver<ProductVerificationResponse> responseObserver)
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
        if (request.getIsAccepted()) //true -> reviewing
        {
            product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
        }
        else // false -> rejected
        {
            product.setApprovalStatus(ApprovalStatusEnum.REJECTED);
        }
        productRepository.save(product); // update product status
        productRepository.flush();

        ProductVerificationResponse response = ProductVerificationResponse.newBuilder()
                .setProductId(product.getId())
                .setApprovalStatus(toProtoApprovalStatus(product.getApprovalStatus()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }



}

