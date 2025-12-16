// java
package org.example.respawnmarket.Service;

import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.*;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

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
