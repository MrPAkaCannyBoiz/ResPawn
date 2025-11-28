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
        List<ProductEntity> entities = productRepository.findPendingProduct();

        List<Product> products = entities.stream()
                .map(this::toProtoProduct)
                .toList();
        GetPendingProductsResponse response = GetPendingProductsResponse.newBuilder()
                .addAllProducts(products)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getProduct(GetProductRequest request,
                                   StreamObserver<GetProductResponse> responseObserver)
    {
        try
        {
            // throw error if product and its related entities not found
            int productId = request.getProductId();
            ProductEntity productEntity = productRepository.findById(productId).orElse(null);
            if (productEntity == null)
            {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Product not found: " + productId)
                        .asRuntimeException());
                return;
            }
            CustomerEntity seller = customerRepository.findById(
                    productEntity.getSeller().getId()).orElse(null);
            PawnshopEntity pawnshop = pawnshopRepository.findById(
                    productEntity.getPawnshop().getId()).orElse(null);

            StringBuilder issues = new StringBuilder();
            if (seller == null) issues.append("seller missing; ");
            if (pawnshop == null) issues.append("pawnshop missing; ");
            if (productEntity.getRegisterDate() == null) issues.append("registerDate missing; ");

            if (!issues.isEmpty())
            {
                responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                        .withDescription("Product has incomplete relations: " + issues.toString().trim())
                        .asRuntimeException());
                return;
            }

            // build response
            assert seller != null;
            assert pawnshop != null;
            Product productDto = toProtoProduct(productEntity);
            NonSensitiveCustomerInfo sellerDto = NonSensitiveCustomerInfo.newBuilder()
                    .setId(seller.getId())
                    .setFirstName(seller.getFirstName())
                    .setLastName(seller.getLastName())
                    .setEmail(seller.getEmail())
                    .setPhoneNumber(seller.getPhoneNumber())
                    .build();
            Pawnshop pawnshopDto = Pawnshop.newBuilder()
                    .setId(pawnshop.getId())
                    .setName(pawnshop.getName())
                    .setAddressId(pawnshop.getAddress().getId())
                    .build();

            GetProductResponse response = GetProductResponse.newBuilder()
                    .setProduct(productDto)
                    .setCustomer(sellerDto)
                    .setPawnshop(pawnshopDto)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e)
        {
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
                .setOtherCategory(entity.getOtherCategory())
                .setPawnshopId(entity.getPawnshop().getId())
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
