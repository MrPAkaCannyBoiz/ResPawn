// java
package org.example.respawnmarket.Service;

import com.respawnmarket.GetAllProductsRequest;
import com.respawnmarket.GetAllProductsResponse;
import com.respawnmarket.ProductWithFirstImage;
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
    void getAllProducts_returnsProductsWithFirstImage() {
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
        class TestObserver implements StreamObserver<GetAllProductsResponse> {
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
}
