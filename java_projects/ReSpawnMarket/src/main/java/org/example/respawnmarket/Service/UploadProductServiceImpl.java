package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.Image;
import com.respawnmarket.UploadProductRequest;
import com.respawnmarket.UploadProductResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import org.example.respawnmarket.Service.ServiceExtensions.ApprovalStatusExtension;
import org.example.respawnmarket.Service.ServiceExtensions.CategoryExtension;
import org.example.respawnmarket.Service.ServiceExtensions.ImageExtension;
import org.example.respawnmarket.entities.CustomerEntity;
import org.example.respawnmarket.entities.ImageEntity;
import org.example.respawnmarket.entities.PawnshopEntity;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.example.respawnmarket.Service.ServiceExtensions.CategoryExtension.toEntityCategory;
import static org.example.respawnmarket.entities.enums.CategoryEnum.*;

@Service
public class UploadProductServiceImpl extends com.respawnmarket.UploadProductServiceGrpc.UploadProductServiceImplBase
{
    // upload product involve product, customer, and image repositories
    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private ImageRepository imageRepository;
    private PawnshopRepository pawnshopRepository;

    @Autowired
    public UploadProductServiceImpl(ProductRepository productRepository,
                                    CustomerRepository customerRepository, ImageRepository imageRepository,
                                    PawnshopRepository pawnshopRepository)
    {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.imageRepository = imageRepository;
        this.pawnshopRepository = pawnshopRepository;
    }

    @Override
    @Transactional // Ensure atomicity of the upload operation
    public void uploadProduct(com.respawnmarket.UploadProductRequest request
            , StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver)
    {
      CustomerEntity givenCustomer = customerRepository
          .findById(request.getSoldByCustomerId())
          .orElse(null);

      if (givenCustomer == null)
      {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription(
                    "Customer not found: " + request.getSoldByCustomerId())
                .asRuntimeException());
        return;
      }

      // 2) Build ProductEntity from request (may return null if validation fails)
      ProductEntity product = getProductEntity(request, responseObserver, givenCustomer);
      if (product == null)
      {
        // getProductEntity already called responseObserver.onError(...)
        return;
      }

      // 3) Initial state for newly uploaded product
      //    - No pawnshop yet (assigned when APPROVED by reseller)
      //    - ApprovalStatus = PENDING
      //    - Sold = false
      product.setPawnshop(null);
      product.setApprovalStatus(ApprovalStatusEnum.PENDING);
      product.setSold(false);

      if (product.getRegisterDate() == null)
      {
        product.setRegisterDate(java.time.LocalDateTime.now());
      }

      // 4) Persist product first (so images can reference it)
      ProductEntity newProduct = productRepository.save(product);

      // 5) Handle images (max 5)
      List<ImageEntity> imageEntities = new ArrayList<>();
      for (String imageUrl : request.getImageUrlList())
      {
        if (imageEntities.size() >= 5)
        {
          responseObserver.onError(
              Status.OUT_OF_RANGE.withDescription(
                      "Cannot upload more than 5 images per product")
                  .asRuntimeException());
          return;
        }

        ImageEntity imageEntity = new ImageEntity(imageUrl, newProduct);
        imageEntities.add(imageEntity);
      }
      imageRepository.saveAll(imageEntities);

      // 6) Map to gRPC Product + response
      Instant instant = newProduct.getRegisterDate()
          .toInstant(java.time.ZoneOffset.UTC);
      Timestamp registerDateTimestamp = Timestamp.newBuilder()
          .setSeconds(instant.getEpochSecond())
          .setNanos(0)
          .build();

      com.respawnmarket.Product productDto =
          com.respawnmarket.Product.newBuilder()
              .setId(newProduct.getId())
              .setName(newProduct.getName())
              .setPrice(newProduct.getPrice())
              .setCondition(newProduct.getCondition())
              .setDescription(newProduct.getDescription())
              .setSoldByCustomerId(newProduct.getSeller().getId())
              .setCategory(request.getCategory())
              .setSold(newProduct.isSold())
              .setApprovalStatus(
                  ApprovalStatusExtension.toProtoApprovalStatus(
                      newProduct.getApprovalStatus()))
              .setRegisterDate(registerDateTimestamp)
              .setOtherCategory(
                  newProduct.getOtherCategory() == null
                      ? ""
                      : newProduct.getOtherCategory())
              // pawnshop_id is omitted here -> will be default 0 until approval
              .build();

      UploadProductResponse response =
          UploadProductResponse.newBuilder()
              .setProduct(productDto)
              .addAllImages(ImageExtension.toProtoImageList(imageEntities))
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    private ProductEntity getProductEntity(UploadProductRequest request,
             StreamObserver<UploadProductResponse> responseObserver, CustomerEntity givenCustomer)
    {
        if (request.getPrice() < 0)
        {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Price must be non-negative")
                    .asRuntimeException());
            return null;
        }
        var product = new ProductEntity(
                request.getName(),
                request.getPrice(),
                request.getCondition(),
                request.getDescription(),
                givenCustomer,
                CategoryExtension.toEntityCategory(request.getCategory()));
        switch (request.getCategory())
        {
            case OTHER ->
            {
                if (request.getOtherCategory().isEmpty())
                {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Other category must be specified when category is OTHER")
                            .asRuntimeException());
                    return null;
                }
                else
                {
                    product.setOtherCategory(request.getOtherCategory());
                }
            }
            case CATEGORY_UNSPECIFIED ->
            {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Category must be specified")
                        .asRuntimeException());
                return null;
            }
            default -> {} // do nothing
        }
        return product;
    }


}
