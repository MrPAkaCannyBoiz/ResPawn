package org.example.respawnmarket.Service;

import com.respawnmarket.Category;
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

public class UploadProductServiceImplTest
{
    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private PawnshopRepository pawnshopRepository;
    private ImageRepository imageRepository;

    private UploadProductServiceImpl service;

    @BeforeEach
    void setUp()
    {
        productRepository = mock(ProductRepository.class);
        customerRepository = mock(CustomerRepository.class);
        pawnshopRepository = mock(PawnshopRepository.class);
        imageRepository = mock(ImageRepository.class);

        service = new UploadProductServiceImpl(
                productRepository,
                customerRepository,
                imageRepository,
                pawnshopRepository
        );
    }

    @Test // ZOMBIES: ONE
    void uploadProductWithOneImage()
    {
        // 1. Arrange
        int customerId = 67;
        CustomerEntity mockCustomer = new CustomerEntity();
        mockCustomer.setId(customerId);

        // Mock finding the customer
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(mockCustomer));

        // Mock saving the product (return the same object with an ID set)
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity p = invocation.getArgument(0);
            p.setId(customerId); // Simulate DB generating ID
            // Ensure date is set for the response builder
            if (p.getRegisterDate() == null) p.setRegisterDate(LocalDateTime.now());
            return p;
        });

        // Create the gRPC request
        com.respawnmarket.UploadProductRequest request = com.respawnmarket.UploadProductRequest.newBuilder()
                .setSoldByCustomerId(customerId)
                .setName("Test Product")
                .setPrice(99.99)
                .setCondition("New")
                .setDescription("Description here")
                .setCategory(com.respawnmarket.Category.ELECTRONICS)
                .addImageUrl("https://example.com/image1.png") // One image
                .build();

        StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver = mock(StreamObserver.class);

        // 2. Act
        service.uploadProduct(request, responseObserver);

        // 3. Assert

        // Verify Product was saved with correct initial states
        ArgumentCaptor<ProductEntity> productCaptor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(productCaptor.capture());
        ProductEntity savedProduct = productCaptor.getValue();

        assertEquals("Test Product", savedProduct.getName());
        assertEquals(ApprovalStatusEnum.PENDING, savedProduct.getApprovalStatus());
        assertFalse(savedProduct.isSold());
        assertEquals(mockCustomer, savedProduct.getSeller());

        // Verify Image was saved linked to the product
        ArgumentCaptor<List<ImageEntity>> imageListCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageRepository).saveAll(imageListCaptor.capture());
        List<ImageEntity> savedImages = imageListCaptor.getValue();

        assertEquals(1, savedImages.size());
        assertEquals("https://example.com/image1.png", savedImages.get(0).getImageUrl());
        assertEquals(savedProduct, savedImages.get(0).getProduct());

        // Verify gRPC response
        verify(responseObserver).onNext(any(com.respawnmarket.UploadProductResponse.class));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

        @Test // ZOMBIES: ZERO and ERROR/EXCEPTION
        void uploadProductWithZeroImages_TriggersError()
        {
            // 1. Arrange
            int customerId = 67;
            CustomerEntity mockCustomer = new CustomerEntity();
            mockCustomer.setId(customerId);

            when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(mockCustomer));

            // Note: The service saves the product BEFORE checking for images, so we must mock the save
            when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
                ProductEntity p = invocation.getArgument(0);
                p.setId(customerId);
                if (p.getRegisterDate() == null) p.setRegisterDate(LocalDateTime.now());
                return p;
            });

            // Create request with NO images
            com.respawnmarket.UploadProductRequest request = com.respawnmarket.UploadProductRequest.newBuilder()
                    .setSoldByCustomerId(customerId)
                    .setName("Zero Image Product")
                    .setPrice(10.00)
                    .setCondition("Used")
                    .setDescription("Description")
                    .setCategory(Category.ELECTRONICS)
                    // No images added
                    .build();

            StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver = mock(StreamObserver.class);

            // 2. Act
            service.uploadProduct(request, responseObserver);

            // 3. Assert
            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(responseObserver).onError(errorCaptor.capture());

            Throwable error = errorCaptor.getValue();
            // Verify the specific error message and status
            assertTrue(error.getMessage().contains("INVALID_ARGUMENT"));
            assertTrue(error.getMessage().contains("At least one image must be provided"));

            // Verify success callbacks were NEVER called
            verify(responseObserver, never()).onNext(any());
            verify(responseObserver, never()).onCompleted();

            // Verify images were NOT saved
            verify(imageRepository, never()).saveAll(any());
        }

    @Test // ZOMBIES: MANY
    void uploadProductWithMultipleImages()
    {
        // 1. Arrange
        int customerId = 67;
        CustomerEntity mockCustomer = new CustomerEntity();
        mockCustomer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(mockCustomer));

        // Mock save to return valid product
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity p = invocation.getArgument(0);
            p.setId(customerId);
            if (p.getRegisterDate() == null) p.setRegisterDate(LocalDateTime.now());
            return p;
        });

        // Create request with 2 images
        com.respawnmarket.UploadProductRequest request = com.respawnmarket.UploadProductRequest.newBuilder()
                .setSoldByCustomerId(customerId)
                .setName("Multiple Image Product")
                .setPrice(50.00)
                .setCondition("Used")
                .setDescription("Description")
                .setCategory(Category.ELECTRONICS)
                .addImageUrl("https://example.com/img1.png")
                .addImageUrl("https://example.com/img2.png")
                .build();

        StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver = mock(StreamObserver.class);

        // 2. Act
        service.uploadProduct(request, responseObserver);

        // 3. Assert
        // Verify Product was saved
        ArgumentCaptor<ProductEntity> productCaptor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(productCaptor.capture());
        ProductEntity savedProduct = productCaptor.getValue();

        // Verify Images were saved
        ArgumentCaptor<List<ImageEntity>> imageListCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageRepository).saveAll(imageListCaptor.capture());
        List<ImageEntity> savedImages = imageListCaptor.getValue();

        // Check size and content
        assertEquals(2, savedImages.size());

        // Verify first image
        assertEquals("https://example.com/img1.png", savedImages.get(0).getImageUrl());
        assertEquals(savedProduct, savedImages.get(0).getProduct());

        // Verify second image
        assertEquals("https://example.com/img2.png", savedImages.get(1).getImageUrl());
        assertEquals(savedProduct, savedImages.get(1).getProduct());

        // Verify response
        verify(responseObserver).onNext(any(com.respawnmarket.UploadProductResponse.class));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

      @Test // ZOMBIES: BOUNDARY AND ERROR/EXCEPTION
      void uploadProductWithTooManyImages_TriggersError()
      {
          // 1. Arrange
          int customerId = 67;
          CustomerEntity mockCustomer = new CustomerEntity();
          mockCustomer.setId(customerId);

          when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(mockCustomer));

          // Mock save to return valid product (Product is saved before image validation)
          when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
              ProductEntity p = invocation.getArgument(0);
              p.setId(customerId);
              if (p.getRegisterDate() == null) p.setRegisterDate(LocalDateTime.now());
              return p;
          });

          // Create request with 6 images (Limit is 5)
          com.respawnmarket.UploadProductRequest.Builder requestBuilder = com.respawnmarket.UploadProductRequest.newBuilder()
                  .setSoldByCustomerId(customerId)
                  .setName("Too Many Images Product")
                  .setPrice(50.00)
                  .setCondition("Used")
                  .setDescription("Description")
                  .setCategory(Category.ELECTRONICS);

          for (int i = 0; i < 6; i++) {
              requestBuilder.addImageUrl("https://example.com/img" + i + ".png");
          }

          StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver = mock(StreamObserver.class);

          // 2. Act
          service.uploadProduct(requestBuilder.build(), responseObserver);

          // 3. Assert
          ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
          verify(responseObserver).onError(errorCaptor.capture());

          Throwable error = errorCaptor.getValue();
          // Verify the specific error message and status
          assertTrue(error.getMessage().contains("OUT_OF_RANGE"));
          assertTrue(error.getMessage().contains("Cannot upload more than 5 images"));

          // Verify success callbacks were NEVER called
          verify(responseObserver, never()).onNext(any());
          verify(responseObserver, never()).onCompleted();

          // Verify images were NOT saved to the DB
          verify(imageRepository, never()).saveAll(any());
      }

    @Test
    void uploadProduct_BlackBox_VerifiesResponseOnly()
    {
        // 1. Arrange (Inputs & Environment Stubbing)
        int customerId = 100;
        CustomerEntity mockCustomer = new CustomerEntity();
        mockCustomer.setId(customerId);

        // Stubbing dependencies is required for the code to run,
        // but in a black-box style test, we treat this as "system configuration"
        // and do not assert on how these mocks were called.
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(mockCustomer));

        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> {
            ProductEntity p = inv.getArgument(0);
            p.setId(12345); // Simulate DB ID generation
            if (p.getRegisterDate() == null) p.setRegisterDate(LocalDateTime.now());
            return p;
        });

        com.respawnmarket.UploadProductRequest request = com.respawnmarket.UploadProductRequest.newBuilder()
                .setSoldByCustomerId(customerId)
                .setName("Black Box Item")
                .setPrice(25.50)
                .setCondition("Mint")
                .setDescription("Boxed")
                .setCategory(Category.ELECTRONICS)
                .addImageUrl("http://img.com/1.jpg")
                .build();

        StreamObserver<com.respawnmarket.UploadProductResponse> responseObserver = mock(StreamObserver.class);

        // 2. Act
        service.uploadProduct(request, responseObserver);

        // 3. Assert (External Outputs Only)
        // We verify the output sent to the client (responseObserver)
        // without inspecting the internal state of the repositories or entities.
        ArgumentCaptor<com.respawnmarket.UploadProductResponse> responseCaptor =
                ArgumentCaptor.forClass(com.respawnmarket.UploadProductResponse.class);

        verify(responseObserver).onNext(responseCaptor.capture());
        com.respawnmarket.UploadProductResponse response = responseCaptor.getValue();

        // Assertions focus on the contract: Input Request -> Output Response
        assertEquals(12345, response.getProduct().getId());
        assertEquals("Black Box Item", response.getProduct().getName());
        assertEquals(com.respawnmarket.ApprovalStatus.PENDING, response.getProduct().getApprovalStatus());
        assertEquals(1, response.getImagesCount());
        assertEquals("http://img.com/1.jpg", response.getImages(0).getUrl());

        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }



}
