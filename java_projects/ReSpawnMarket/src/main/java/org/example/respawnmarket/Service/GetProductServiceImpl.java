package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.Service.ServiceExtensions.ImageExtension;
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

    @Autowired
    public GetProductServiceImpl(ProductRepository productRepository, CustomerRepository customerRepository,
                                 PawnshopRepository pawnshopRepository, ImageRepository imageRepository)
    {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.pawnshopRepository = pawnshopRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public void getPendingProducts(GetPendingProductsRequest request,
                                   StreamObserver<GetPendingProductsResponse> responseObserver)
    {
        List<ProductEntity> entities = productRepository.findPendingProduct();
        for (ProductEntity entity : entities)
        {
            checkNullAndRelations(entity, responseObserver);
        }

        List<ProductWithFirstImage> products = entities.stream()
                .map(this::toProtoProductWithImage)
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
            int productId = request.getProductId();
            ProductEntity productEntity = productRepository.findById(productId).orElse(null);
            checkNullAndRelations(productEntity, responseObserver); // throws error if null
            List<ImageEntity> imageEntities = imageRepository.findAllByProductId(productId);

            assert productEntity != null;
            CustomerEntity seller = customerRepository.findById(
                    productEntity.getSeller().getId()).orElse(null);
            PawnshopEntity pawnshop = pawnshopRepository.findById(
                    productEntity.getPawnshop().getId()).orElse(null);

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
            Address addressDto = Address.newBuilder()
                    .setId(pawnshop.getAddress().getId())
                    .setStreetName(pawnshop.getAddress().getStreetName())
                    .setSecondaryUnit(pawnshop.getAddress().getSecondaryUnit())
                    .setPostalCode(pawnshop.getAddress().getPostal().getPostalCode())
                    .build();
            Postal postalDto = Postal.newBuilder()
                    .setPostalCode(pawnshop.getAddress().getPostal().getPostalCode())
                    .setCity(pawnshop.getAddress().getPostal().getCity())
                    .build();

            GetProductResponse response = GetProductResponse.newBuilder()
                    .setProduct(productDto)
                    .addAllImages(ImageExtension.toProtoImageList(imageEntities))
                    .setCustomer(sellerDto)
                    .setPawnshop(pawnshopDto)
                    .setPawnshopAddress(addressDto)
                    .setPawnshopPostal(postalDto)
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
        for (ProductEntity entity : entities)
        {
            checkNullAndRelations(entity, responseObserver);
        }

        List<ProductWithFirstImage> products = entities.stream()
                .map(this::toProtoProductWithImage)
                .toList();
        GetAllProductsResponse response = GetAllProductsResponse.newBuilder()
                .addAllProducts(products)
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
                .setPawnshopId(entity.getPawnshop().getId())
                .build();
        return productDto;
    }

    private void checkNullAndRelations(ProductEntity productEntity, StreamObserver<?> responseObserver)
    {
        if (productEntity == null)
        {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Product not found")
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
        }
    }


}
