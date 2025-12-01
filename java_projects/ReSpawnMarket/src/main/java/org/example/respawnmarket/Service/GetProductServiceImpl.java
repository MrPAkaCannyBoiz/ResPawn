package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.CustomerEntity;
import org.example.respawnmarket.entities.PawnshopEntity;
import org.example.respawnmarket.entities.ProductEntity;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

// TODO: add two addresses of customer in response
@Service
public class GetProductServiceImpl extends GetProductServiceGrpc.GetProductServiceImplBase
{
    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private PawnshopRepository pawnshopRepository;

    @Autowired
    public GetProductServiceImpl(ProductRepository productRepository, CustomerRepository customerRepository,
                                 PawnshopRepository pawnshopRepository)
    {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.pawnshopRepository = pawnshopRepository;
    }

    @Override
    public void getPendingProducts(GetPendingProductsRequest request,
                                   StreamObserver<GetPendingProductsResponse> responseObserver)
    {
      try {
        List<ProductEntity> entities = productRepository.findPendingProduct();

        List<Product> products = entities.stream()
            .map(entity -> {
              try {
                return toProtoProduct(entity);
              } catch (Exception e) {
                // Wrap with product id so we know which one failed
                throw new RuntimeException("Failed to map product id=" + entity.getId(), e);
              }
            })
            .toList();

        GetPendingProductsResponse response = GetPendingProductsResponse.newBuilder()
            .addAllProducts(products)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
      catch (Exception e) {
        e.printStackTrace();  // <— now you’ll get a stack trace in the Java console

        responseObserver.onError(
            io.grpc.Status.UNKNOWN
                .withDescription("getPendingProducts failed: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
      }
    }

    @Override
    public void getProduct(GetProductRequest request,
                                   StreamObserver<GetProductResponse> responseObserver)
    {
      try
      {
        int productId = request.getProductId();
        ProductEntity productEntity = productRepository.findById(productId).orElse(null);
        if (productEntity == null)
        {
          responseObserver.onError(io.grpc.Status.NOT_FOUND
              .withDescription("Product not found: " + productId)
              .asRuntimeException());
          return;
        }

        // Seller is still required
        CustomerEntity seller = productEntity.getSeller();
        if (seller == null)
        {
          responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
              .withDescription("Product " + productId + " has no seller.")
              .asRuntimeException());
          return;
        }

        Product productDto = toProtoProduct(productEntity);

        NonSensitiveCustomerInfo sellerDto = NonSensitiveCustomerInfo.newBuilder()
            .setId(seller.getId())
            .setFirstName(seller.getFirstName())
            .setLastName(seller.getLastName())
            .setEmail(seller.getEmail())
            .setPhoneNumber(seller.getPhoneNumber())
            .build();

        GetProductResponse.Builder builder = GetProductResponse.newBuilder()
            .setProduct(productDto)
            .setCustomer(sellerDto);

        // Pawnshop is OPTIONAL (only set after approval)
        PawnshopEntity pawnshop = productEntity.getPawnshop();
        if (pawnshop != null &&
            pawnshop.getAddress() != null &&
            pawnshop.getAddress().getPostal() != null)
        {
          Pawnshop pawnshopDto = Pawnshop.newBuilder()
              .setId(pawnshop.getId())
              .setName(pawnshop.getName())
              .setAddressId(pawnshop.getAddress().getId())
              .build();

          Address addressDto = Address.newBuilder()
              .setId(pawnshop.getAddress().getId())
              .setStreetName(pawnshop.getAddress().getStreetName())
              .setSecondaryUnit(
                  pawnshop.getAddress().getSecondaryUnit() == null
                      ? ""
                      : pawnshop.getAddress().getSecondaryUnit())
              .setPostalCode(pawnshop.getAddress().getPostal().getPostalCode())
              .build();

          Postal postalDto = Postal.newBuilder()
              .setPostalCode(pawnshop.getAddress().getPostal().getPostalCode())
              .setCity(pawnshop.getAddress().getPostal().getCity())
              .build();

          builder
              .setPawnshop(pawnshopDto)
              .setPawnshopAddress(addressDto)
              .setPawnshopPostal(postalDto);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
      }
      catch (Exception e)
      {
        e.printStackTrace(); // keep this while debugging so you see the real cause
        responseObserver.onError(io.grpc.Status.INTERNAL
            .withDescription("Failed to get product")
            .withCause(e)
            .asRuntimeException());
      }
    }

    public void getAllProducts(GetAllProductsRequest request,
                                   StreamObserver<GetAllProductsResponse> responseObserver)
    {
        List<ProductEntity> entities = productRepository.findAll();

        List<Product> products = entities.stream()
                .map(this::toProtoProduct)
                .toList();
        GetAllProductsResponse response = GetAllProductsResponse.newBuilder()
                .addAllProducts(products)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Product toProtoProduct(ProductEntity entity)
    {
      ApprovalStatus approvalStatus = toProtoApprovalStatus(entity.getApprovalStatus());
      Category category = toProtoCategory(entity.getCategory());

      // Seller might be null for some products
      int sellerId = entity.getSeller() != null ? entity.getSeller().getId() : 0;

      // Pawnshop is null for pending products
      int pawnshopId = entity.getPawnshop() != null ? entity.getPawnshop().getId() : 0;

      // RegisterDate might be null for some pending products
      Instant instant;
      if (entity.getRegisterDate() != null) {
        instant = entity.getRegisterDate().toInstant(java.time.ZoneOffset.UTC);
      } else {
        instant = Instant.now(); // or throw a clearer error if you prefer
      }

      Timestamp registerDateTimestamp = Timestamp.newBuilder()
          .setSeconds(instant.getEpochSecond())
          .setNanos(0)
          .build();

      return Product.newBuilder()
          .setId(entity.getId())
          .setName(entity.getName())
          .setPrice(entity.getPrice())
          .setCondition(entity.getCondition())
          .setDescription(entity.getDescription() == null ? "" : entity.getDescription())
          .setPhotoUrl(entity.getPhotoUrl() == null ? "" : entity.getPhotoUrl())
          .setSoldByCustomerId(sellerId)
          .setCategory(category)
          .setSold(entity.isSold())
          .setApprovalStatus(approvalStatus)
          .setRegisterDate(registerDateTimestamp)
          .setOtherCategory(entity.getOtherCategory() == null ? "" : entity.getOtherCategory())
          .setPawnshopId(pawnshopId)
          .build();
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
