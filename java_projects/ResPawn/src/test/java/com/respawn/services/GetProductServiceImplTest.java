// java
package com.respawn.services;

import com.respawn.dtos.ProductInspectionDTO;
import com.respawn.entities.*;
import com.respawn.repositories.CustomerRepository;
import com.respawn.repositories.ImageRepository;
import com.respawn.repositories.PawnshopRepository;
import com.respawn.repositories.ProductRepository;
import com.respawnmarket.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import com.respawn.entities.*;
import com.respawn.entities.enums.ApprovalStatusEnum;
import com.respawn.entities.enums.CategoryEnum;
import com.respawn.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductServiceImplTest {

    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private PawnshopRepository pawnshopRepository;
    private ImageRepository imageRepository;

    private GetProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        customerRepository = mock(CustomerRepository.class);
        pawnshopRepository = mock(PawnshopRepository.class);
        imageRepository = mock(ImageRepository.class);

        service = new GetProductServiceImpl(
                productRepository,
                customerRepository,
                pawnshopRepository,
                imageRepository
        );
    }

    @Test
    void getAllProducts_returnsProductsWithFirstImage()
    {
        // arrange: build entities that satisfy checkNullAndRelations()
        CustomerEntity seller = new CustomerEntity();
        seller.setId(1);
        seller.setFirstName("John");
        seller.setLastName("Doe");
        seller.setEmail("john@example.com");
        seller.setPhoneNumber("123456");

        ProductEntity product = new ProductEntity();
        product.setId(10);
        product.setName("Test product");
        product.setPrice(100.0);
        product.setCondition("NEW");
        product.setDescription("Desc");
        product.setSeller(seller);
        product.setCategory(CategoryEnum.ELECTRONICS);
        product.setSold(false);
        product.setApprovalStatus(ApprovalStatusEnum.PENDING);
        product.setRegisterDate(LocalDateTime.now());
        product.setPawnshop(null);

        ImageEntity image = new ImageEntity();
        image.setId(99);
        image.setImageUrl("https://example.com/img.jpg");
        image.setProduct(product);

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(customerRepository.findById(seller.getId())).thenReturn(java.util.Optional.of(seller));
        // pawnshop is null here, which is allowed because product is APPROVED
        when(imageRepository.findAllByProductId(product.getId()))
                .thenReturn(List.of(image));

        // custom StreamObserver to capture response
        class TestObserver implements StreamObserver<GetAllProductsResponse>
        {
            GetAllProductsResponse response;
            Throwable error;
            boolean completed = false;

            @Override
            public void onNext(GetAllProductsResponse value) {
                this.response = value;
            }

            @Override
            public void onError(Throwable t) {
                this.error = t;
            }

            @Override
            public void onCompleted() {
                this.completed = true;
            }
        }

        TestObserver observer = new TestObserver();

        // act
        service.getAllProducts(GetAllProductsRequest.getDefaultInstance(), observer);

        // assert
        assertNull(observer.error, "Should not have error");
        assertTrue(observer.completed, "Should be completed");
        assertNotNull(observer.response, "Response should not be null");

        assertEquals(1, observer.response.getProductsCount());
        ProductWithFirstImage pfi = observer.response.getProducts(0);
        assertEquals(product.getId(), pfi.getProduct().getId());
        assertTrue(pfi.hasFirstImage());
        assertEquals(image.getId(), pfi.getFirstImage().getId());
        assertEquals(image.getImageUrl(), pfi.getFirstImage().getUrl());
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private CustomerEntity buildSeller(int id) {
        CustomerEntity seller = new CustomerEntity();
        seller.setId(id);
        seller.setFirstName("John");
        seller.setLastName("Doe");
        seller.setEmail("john@example.com");
        seller.setPhoneNumber("123456");
        return seller;
    }

    private ProductEntity buildProduct(int id, CustomerEntity seller,
                                       ApprovalStatusEnum status, PawnshopEntity pawnshop) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setName("Widget");
        p.setPrice(99.0);
        p.setCondition("GOOD");
        p.setDescription("A widget");
        p.setSeller(seller);
        p.setCategory(CategoryEnum.ELECTRONICS);
        p.setSold(false);
        p.setApprovalStatus(status);
        p.setRegisterDate(LocalDateTime.now());
        p.setPawnshop(pawnshop);
        return p;
    }

    private ImageEntity buildImage(int id, ProductEntity product) {
        ImageEntity img = new ImageEntity();
        img.setId(id);
        img.setImageUrl("http://img.example.com/" + id + ".jpg");
        img.setProduct(product);
        return img;
    }

    // ── getPendingProducts ────────────────────────────────────────────────

    @Test
    void getPendingProducts_returnsPendingProducts() {
        CustomerEntity seller = buildSeller(1);
        // PENDING products don't require a pawnshop
        ProductEntity product = buildProduct(10, seller, ApprovalStatusEnum.PENDING, null);
        ImageEntity image = buildImage(99, product);

        when(productRepository.findPendingProduct()).thenReturn(List.of(product));
        when(customerRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(imageRepository.findAllByProductId(product.getId())).thenReturn(List.of(image));

        StreamObserver<GetPendingProductsResponse> responseObserver = mock(StreamObserver.class);
        service.getPendingProducts(GetPendingProductsRequest.getDefaultInstance(), responseObserver);

        ArgumentCaptor<GetPendingProductsResponse> captor =
                ArgumentCaptor.forClass(GetPendingProductsResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        assertEquals(1, captor.getValue().getProductsCount());
    }

    // ── getAllAvailableProducts ────────────────────────────────────────────

    @Test
    void getAllAvailableProducts_returnsProducts() {
        CustomerEntity seller = buildSeller(2);
        ProductEntity product = buildProduct(20, seller, ApprovalStatusEnum.PENDING, null);
        ImageEntity image = buildImage(200, product);

        when(productRepository.findAllAvailableProducts()).thenReturn(List.of(product));
        when(customerRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(imageRepository.findAllByProductId(product.getId())).thenReturn(List.of(image));

        StreamObserver<GetAllAvailableProductsResponse> responseObserver = mock(StreamObserver.class);
        service.getAllAvailableProducts(GetAllAvailableProductsRequest.getDefaultInstance(), responseObserver);

        verify(responseObserver).onNext(any());
        verify(responseObserver).onCompleted();
    }

    // ── getAllReviewingProducts ────────────────────────────────────────────

    @Test
    void getAllReviewingProducts_returnsProducts() {
        CustomerEntity seller = buildSeller(3);
        ProductEntity product = buildProduct(30, seller, ApprovalStatusEnum.REVIEWING, null);
        ImageEntity image = buildImage(300, product);

        when(productRepository.findAllReviewingProducts()).thenReturn(List.of(product));
        when(customerRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(imageRepository.findAllByProductId(product.getId())).thenReturn(List.of(image));

        StreamObserver<GetAllReviewingProductsResponse> responseObserver = mock(StreamObserver.class);
        service.getAllReviewingProducts(GetAllReviewingProductsRequest.getDefaultInstance(), responseObserver);

        verify(responseObserver).onNext(any());
        verify(responseObserver).onCompleted();
    }

    // ── getLatestInspection ───────────────────────────────────────────────

    @Test
    void getLatestInspection_emptyList_returnsNotFound() {
        when(productRepository.findProductsWithLatestInspection(anyInt())).thenReturn(List.of());

        StreamObserver<GetLatestProductInspectionResponse> responseObserver = mock(StreamObserver.class);
        service.getLatestInspection(
                GetLatestProductInspectionRequest.newBuilder().setCustomerId(1).build(),
                responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        assertEquals(Status.NOT_FOUND.getCode(),
                ((StatusRuntimeException) errorCaptor.getValue()).getStatus().getCode());
    }

    @Test
    void getLatestInspection_validDtos_returnsResponse() {
        CustomerEntity seller = buildSeller(5);
        ProductEntity product = buildProduct(50, seller, ApprovalStatusEnum.APPROVED, null);
        ImageEntity image = buildImage(500, product);

        ProductInspectionDTO dto = new ProductInspectionDTO(product, "Looks good");

        when(productRepository.findProductsWithLatestInspection(5)).thenReturn(List.of(dto));
        when(imageRepository.findAllByProductId(product.getId())).thenReturn(List.of(image));

        StreamObserver<GetLatestProductInspectionResponse> responseObserver = mock(StreamObserver.class);
        service.getLatestInspection(
                GetLatestProductInspectionRequest.newBuilder().setCustomerId(5).build(),
                responseObserver);

        ArgumentCaptor<GetLatestProductInspectionResponse> captor =
                ArgumentCaptor.forClass(GetLatestProductInspectionResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        assertEquals(1, captor.getValue().getProductsCount());
        assertEquals("Looks good", captor.getValue().getProducts(0).getInspectionComments());
    }

    // ── getProduct branches ───────────────────────────────────────────────

    @Test
    void getProduct_productNotFound_sendsError() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        StreamObserver<GetProductResponse> responseObserver = mock(StreamObserver.class);
        service.getProduct(GetProductRequest.newBuilder().setProductId(99).build(), responseObserver);

        verify(responseObserver, atLeastOnce()).onError(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void getProduct_sellerNotFound_sendsNotFoundError() {
        CustomerEntity seller = buildSeller(2);
        ProductEntity product = buildProduct(20, seller, ApprovalStatusEnum.PENDING, null);

        when(productRepository.findById(20)).thenReturn(Optional.of(product));
        // checkNullAndRelations calls findById(seller), getProduct also calls it — both return empty
        when(customerRepository.findById(seller.getId())).thenReturn(Optional.empty());
        when(imageRepository.findAllByProductId(20)).thenReturn(List.of());

        StreamObserver<GetProductResponse> responseObserver = mock(StreamObserver.class);
        service.getProduct(GetProductRequest.newBuilder().setProductId(20).build(), responseObserver);

        verify(responseObserver, atLeastOnce()).onError(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void getProduct_noPawnshop_returnsPendingProductWithoutPawnshop() {
        CustomerEntity seller = buildSeller(2);
        // PENDING product with no pawnshop — should succeed
        ProductEntity product = buildProduct(21, seller, ApprovalStatusEnum.PENDING, null);
        ImageEntity image = buildImage(210, product);

        when(productRepository.findById(21)).thenReturn(Optional.of(product));
        when(customerRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(imageRepository.findAllByProductId(21)).thenReturn(List.of(image));

        StreamObserver<GetProductResponse> responseObserver = mock(StreamObserver.class);
        service.getProduct(GetProductRequest.newBuilder().setProductId(21).build(), responseObserver);

        ArgumentCaptor<GetProductResponse> captor = ArgumentCaptor.forClass(GetProductResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        assertFalse(captor.getValue().hasPawnshop());
    }

    @Test
    void getProduct_returnsProductDetails_whenProductExists()
    {
        // Arrange
        int productId = 20;
        int sellerId = 2;
        int pawnshopId = 5;

        // 1. Setup Seller
        CustomerEntity seller = new CustomerEntity();
        seller.setId(sellerId);
        seller.setFirstName("Jane");
        seller.setLastName("Smith");
        seller.setEmail("jane@example.com");
        seller.setPhoneNumber("555-0199");
        seller.setPassword("password1234");

        // 2. Setup Pawnshop (Required for APPROVED products based on checkNullAndRelations logic)
        PostalEntity postal = new PostalEntity();
        postal.setPostalCode(90210);
        postal.setCity("Beverly Hills");

        AddressEntity address = new AddressEntity();
        address.setId(55);
        address.setStreetName("Rodeo Dr");
        address.setSecondaryUnit("Suite 100");
        address.setPostal(postal);

        PawnshopEntity pawnshop = new PawnshopEntity();
        pawnshop.setId(pawnshopId);
        pawnshop.setName("Luxury Pawn");
        pawnshop.setAddress(address);

        // 3. Setup Product
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setName("Diamond Ring");
        product.setPrice(5000.0);
        product.setCondition("LIKE_NEW");
        product.setDescription("Shiny");
        product.setSeller(seller);
        product.setCategory(CategoryEnum.ELECTRONICS); // Using existing enum
        product.setSold(false);
        product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
        product.setRegisterDate(LocalDateTime.now());
        product.setPawnshop(pawnshop);

        // 4. Setup Images
        ImageEntity image = new ImageEntity();
        image.setId(200);
        image.setImageUrl("http://images.com/ring.jpg");
        image.setProduct(product);

        // 5. Mock Repository Calls
        // Note: These are called multiple times (in checkNullAndRelations and getProduct),
        // but Mockito handles multiple calls to the same mock by default.
        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));
        when(customerRepository.findById(sellerId)).thenReturn(java.util.Optional.of(seller));
        when(pawnshopRepository.findById(pawnshopId)).thenReturn(java.util.Optional.of(pawnshop));
        when(imageRepository.findAllByProductId(productId)).thenReturn(List.of(image));

        // 6. Prepare Observer
        StreamObserver<GetProductResponse> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<GetProductResponse> responseCaptor = ArgumentCaptor.forClass(GetProductResponse.class);

        // Act
        GetProductRequest request = GetProductRequest.newBuilder().setProductId(productId).build();
        service.getProduct(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        GetProductResponse response = responseCaptor.getValue();
        assertNotNull(response);
        assertEquals(productId, response.getProduct().getId());
        assertEquals("Diamond Ring", response.getProduct().getName());
        assertEquals(sellerId, response.getCustomer().getId());

        // Verify Pawnshop details were mapped
        assertTrue(response.hasPawnshop());
        assertEquals(pawnshopId, response.getPawnshop().getId());
        assertEquals("Luxury Pawn", response.getPawnshop().getName());

        // Verify Images
        assertEquals(1, response.getImagesCount());
        assertEquals("http://images.com/ring.jpg", response.getImages(0).getUrl());
    }




}
