package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.Service.ServiceExtensions.ImageExtension;
import org.example.respawnmarket.dtos.ProductInspectionDTO;
import org.example.respawnmarket.entities.CustomerEntity;
import org.example.respawnmarket.entities.ImageEntity;
import org.example.respawnmarket.entities.PawnshopEntity;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.respawnmarket.Service.ServiceExtensions.ApprovalStatusExtension.toProtoApprovalStatus;
import static org.example.respawnmarket.Service.ServiceExtensions.CategoryExtension.toProtoCategory;

// TODO: add two addresses of customer in response
@Service
public class GetProductServiceImpl extends GetProductServiceGrpc.GetProductServiceImplBase
{
  private ProductRepository productRepository;
  private CustomerRepository customerRepository;
  private PawnshopRepository pawnshopRepository;
  private ImageRepository imageRepository;

  @Autowired public GetProductServiceImpl(ProductRepository productRepository,
      CustomerRepository customerRepository,
      PawnshopRepository pawnshopRepository, ImageRepository imageRepository)
  {
    this.productRepository = productRepository;
    this.customerRepository = customerRepository;
    this.pawnshopRepository = pawnshopRepository;
    this.imageRepository = imageRepository;
  }

  @Override public void getPendingProducts(GetPendingProductsRequest request,
      StreamObserver<GetPendingProductsResponse> responseObserver)
  {
    List<ProductEntity> entities = productRepository.findPendingProduct();
    for (ProductEntity entity : entities)
    {
      checkNullAndRelations(entity, responseObserver);
    }

    List<ProductWithFirstImage> products = entities.stream()
        .map(this::toProtoProductWithImage).toList();
    GetPendingProductsResponse response = GetPendingProductsResponse.newBuilder()
        .addAllProducts(products).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override public void getProduct(GetProductRequest request,
      StreamObserver<GetProductResponse> responseObserver)
  {
    try
    {
      int productId = request.getProductId();
      ProductEntity productEntity = productRepository.findById(productId)
          .orElse(null);

      // may send error + return if product/seller/pawnshop invalid
      checkNullAndRelations(productEntity, responseObserver);

      List<ImageEntity> imageEntities =
          imageRepository.findAllByProductId(productId);

      // 1) Map product entity -> proto Product
      Product productDto = toProtoProduct(productEntity);

      // 2) Load seller
      CustomerEntity seller = customerRepository
          .findById(productEntity.getSeller().getId())
          .orElse(null);

      if (seller == null)
      {
        responseObserver.onError(
            io.grpc.Status.NOT_FOUND
                .withDescription("Seller not found for product " + productId)
                .asRuntimeException());
        return;
      }

      // 3) Map seller -> proto NonSensitiveCustomerInfo
      NonSensitiveCustomerInfo sellerDto = NonSensitiveCustomerInfo.newBuilder()
          .setId(seller.getId())
          .setFirstName(seller.getFirstName())
          .setLastName(seller.getLastName())
          .setEmail(seller.getEmail())
          .setPhoneNumber(seller.getPhoneNumber())
          .build();

      // 4) Load pawnshop only if present
      PawnshopEntity pawnshop = null;
      if (productEntity.getPawnshop() != null)
      {
        pawnshop = pawnshopRepository.findById(
            productEntity.getPawnshop().getId()).orElse(null);
      }

      Pawnshop pawnshopDto = null;
      Address addressDto = null;
      Postal postalDto = null;

      if (pawnshop != null && pawnshop.getAddress() != null)
      {
        pawnshopDto = Pawnshop.newBuilder()
            .setId(pawnshop.getId())
            .setName(pawnshop.getName())
            .setAddressId(pawnshop.getAddress().getId())
            .build();

        addressDto = Address.newBuilder()
            .setId(pawnshop.getAddress().getId())
            .setStreetName(pawnshop.getAddress().getStreetName())
            .setSecondaryUnit(pawnshop.getAddress().getSecondaryUnit())
            .setPostalCode(pawnshop.getAddress().getPostal().getPostalCode())
            .build();

        postalDto = Postal.newBuilder()
            .setPostalCode(pawnshop.getAddress().getPostal().getPostalCode())
            .setCity(pawnshop.getAddress().getPostal().getCity())
            .build();
      }

      // 5) Build response
      GetProductResponse.Builder builder = GetProductResponse.newBuilder()
          .setProduct(productDto)
          .addAllImages(ImageExtension.toProtoImageList(imageEntities))
          .setCustomer(sellerDto); // now this compiles

      if (pawnshopDto != null)
      {
        builder
            .setPawnshop(pawnshopDto)
            .setPawnshopAddress(addressDto)
            .setPawnshopPostal(postalDto);
      }

      GetProductResponse response = builder.build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
    catch (Exception e)
    {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Failed to get product" + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    }
  }

    @Override
    public void getAllProducts(GetAllProductsRequest request,
                                   StreamObserver<GetAllProductsResponse> responseObserver)
    {
        List<ProductEntity> entities = productRepository.findAll();
        List<ProductWithFirstImage> products = toProtoProductWithImageList(entities, responseObserver);
        GetAllProductsResponse response = GetAllProductsResponse.newBuilder()
                .addAllProducts(products)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAvailableProducts(GetAllAvailableProductsRequest request,
                                   StreamObserver<GetAllAvailableProductsResponse> responseObserver)
    {
        // available products are products that are not sold and approved
        List<ProductEntity> entities = productRepository.findAllAvailableProducts();
        List<ProductWithFirstImage> products = toProtoProductWithImageList(entities, responseObserver);
        GetAllAvailableProductsResponse response = GetAllAvailableProductsResponse.newBuilder()
                .addAllProducts(products)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void getAllReviewingProducts(GetAllReviewingProductsRequest request,
                                   StreamObserver<GetAllReviewingProductsResponse> responseObserver)
    {
        List<ProductEntity> entities = productRepository.findAllReviewingProducts();
        List<ProductWithFirstImage> products = toProtoProductWithImageList(entities, responseObserver);
        GetAllReviewingProductsResponse response = GetAllReviewingProductsResponse.newBuilder()
                .addAllProducts(products)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

      @Override
      public void getLatestInspection(GetLatestProductInspectionRequest request,
                                      StreamObserver<GetLatestProductInspectionResponse> responseObserver)
      {
        List<ProductInspectionDTO> inspectionDTOs = productRepository.findProductsWithLatestInspection(
            request.getCustomerId());
        if (inspectionDTOs.isEmpty())
        {
          responseObserver.onError(Status.NOT_FOUND.withDescription(
                  "No inspections found for product ID: " + request.getCustomerId())
              .asRuntimeException());
          return;
        }
        for (ProductInspectionDTO inspectionDTO : inspectionDTOs)
        {
          if (inspectionDTO.getLatestComment() == null)
          {
            // set it to empty string if no inspection found
            inspectionDTO.setLatestComment("");
          }
          if (inspectionDTO.getProduct() == null)
          {
            responseObserver.onError(Status.NOT_FOUND.withDescription(
                    "Product not found with ID: " + request.getCustomerId())
                .asRuntimeException());
            return;
          }
        }
        List<LatestProductFromInspection> latestProducts = inspectionDTOs.stream()
            .map(this::toProtoProductViaDto)
            .toList();
        GetLatestProductInspectionResponse response = GetLatestProductInspectionResponse.newBuilder()
            .addAllProducts(latestProducts)
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();


      }






    private ProductWithFirstImage toProtoProductWithImage(ProductEntity entity)
    {
        Product productDto = toProtoProduct(entity);
        List<ImageEntity> images = imageRepository.findAllByProductId(entity.getId());
        Image firstImageDto = null;
        if (images != null && !images.isEmpty())
        {
            ImageEntity firstImage = images.getFirst();
            firstImageDto = Image.newBuilder()
                    .setId(firstImage.getId())
                    .setUrl(firstImage.getImageUrl())
                    .setProductId(firstImage.getProduct().getId())
                    .build();
        }
        ProductWithFirstImage.Builder builder = ProductWithFirstImage
                .newBuilder()
                .setProduct(productDto);
        if (firstImageDto != null)
        {
            builder.setFirstImage(firstImageDto);
        }
        return builder.build();
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

        Product productDto = Product.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setPrice(entity.getPrice())
                .setCondition(entity.getCondition())
                .setDescription(entity.getDescription())
                .setSoldByCustomerId(entity.getSeller().getId())
                .setCategory(category)
                .setSold(entity.isSold())
                .setApprovalStatus(approvalStatus)
                .setRegisterDate(registerDateTimestamp)
                .setOtherCategory(entity.getOtherCategory() == null ? "" : entity.getOtherCategory())
                .setPawnshopId(entity.getPawnshop() != null ? entity.getPawnshop().getId() : 0)
                .build();
        return productDto;
    }

  private void checkNullAndRelations(ProductEntity productEntity,
      StreamObserver<?> responseObserver)
  {
      if (productEntity == null)
      {
          responseObserver.onError(
                  io.grpc.Status.NOT_FOUND.withDescription("Product not found")
                          .asRuntimeException());
          return;
      }

      CustomerEntity seller = customerRepository.findById(
              productEntity.getSeller().getId()).orElse(null);

      PawnshopEntity pawnshop = null;
      if (productEntity.getPawnshop() != null)
      {
          pawnshop = pawnshopRepository.findById(
                  productEntity.getPawnshop().getId()).orElse(null);
      }

      boolean isPending =
              productEntity.getApprovalStatus() == ApprovalStatusEnum.PENDING;

      StringBuilder issues = new StringBuilder();
      if (seller == null)
          issues.append("seller missing");
      // Only require pawnshop when NOT pending
      if (!isPending && pawnshop == null)
          issues.append("pawnshop missing on non-pending product");
      if (productEntity.getRegisterDate() == null)
          issues.append("registerDate missing");

      if (!issues.isEmpty())
      {
          responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                  .withDescription("Product has incomplete relations: " + issues.toString().trim())
                  .asRuntimeException());
      }
    }

    private List<ProductWithFirstImage> toProtoProductWithImageList(List<ProductEntity> entities,
                                                                    StreamObserver<?> responseObserver)
    {
        if (entities == null)
        {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Products not found")
                    .asRuntimeException());
        }
        assert entities != null;
        for (ProductEntity entity : entities)
        {
            checkNullAndRelations(entity, responseObserver);
        }

        List<ProductWithFirstImage> products = entities.stream()
                .map(this::toProtoProductWithImage)
                .toList();
        return products;
    }

  private List<ProductWithFirstImage> toProtoProductWithImageListGivenProductInspection(
      List<ProductInspectionDTO> dtos,
      StreamObserver<?> responseObserver)
  {
    if (dtos == null)
    {
      responseObserver.onError(io.grpc.Status.NOT_FOUND
          .withDescription("Products not found")
          .asRuntimeException());
    }
    assert dtos != null;
    List<ProductEntity> entities = new ArrayList<>();
    for (ProductInspectionDTO dto : dtos)
    {
      checkNullAndRelations(dto.getProduct(), responseObserver);
      entities.add(dto.getProduct());
    }

    List<ProductWithFirstImage> products = entities.stream()
        .map(this::toProtoProductWithImage)
        .toList();
    return products;
  }

  private LatestProductFromInspection toProtoProductViaDto(ProductInspectionDTO dto)
  {
    ApprovalStatus approvalStatus = toProtoApprovalStatus(dto.getProduct().getApprovalStatus());
    Category category = toProtoCategory(dto.getProduct().getCategory());
    Instant instant = dto.getProduct().getRegisterDate().toInstant(java.time.ZoneOffset.UTC);
    Timestamp registerDateTimestamp = Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(0)
        .build();

    Product productDto = Product.newBuilder()
        .setId(dto.getProduct().getId())
        .setName(dto.getProduct().getName())
        .setPrice(dto.getProduct().getPrice())
        .setCondition(dto.getProduct().getCondition())
        .setDescription(dto.getProduct().getDescription())
        .setSoldByCustomerId(dto.getProduct().getSeller().getId())
        .setCategory(category)
        .setSold(dto.getProduct().isSold())
        .setApprovalStatus(approvalStatus)
        .setRegisterDate(registerDateTimestamp)
        .setOtherCategory(dto.getProduct().getOtherCategory() == null ? "" : dto.getProduct().getOtherCategory())
        .setPawnshopId(dto.getProduct().getPawnshop() != null ? dto.getProduct().getPawnshop().getId() : 0)
        .build();
    Image firstImageDto = null;
    List<ImageEntity> images = imageRepository.findAllByProductId(dto.getProduct().getId());
    firstImageDto = ImageExtension.toProtoImageList(images).getFirst();
    ProductWithFirstImage productWithImageDto = ProductWithFirstImage.newBuilder()
        .setProduct(productDto)
        .setFirstImage(firstImageDto)
        .build();
    LatestProductFromInspection latestProductDto = LatestProductFromInspection.newBuilder()
        .setProduct(productWithImageDto)
        .setInspectionComments(dto.getLatestComment())
        .build();
    return latestProductDto;
  }



}

