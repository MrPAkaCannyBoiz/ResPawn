package com.respawn.services;

import com.respawn.entities.InspectionEntity;
import com.respawn.entities.PawnshopEntity;
import com.respawn.entities.ProductEntity;
import com.respawn.entities.ResellerEntity;
import com.respawn.entities.enums.ApprovalStatusEnum;
import com.respawn.repositories.InspectionRepository;
import com.respawn.repositories.PawnshopRepository;
import com.respawn.repositories.ProductRepository;
import com.respawn.repositories.ResellerRepository;
import com.respawnmarket.ProductInspectionRequest;
import com.respawnmarket.ProductInspectionResponse;
import com.respawnmarket.ProductVerificationRequest;
import com.respawnmarket.ProductVerificationResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductInspectionServiceImplTest {

    private ProductRepository productRepository;
    private InspectionRepository inspectionRepository;
    private ResellerRepository resellerRepository;
    private PawnshopRepository pawnshopRepository;
    private ProductInspectionServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        inspectionRepository = mock(InspectionRepository.class);
        resellerRepository = mock(ResellerRepository.class);
        pawnshopRepository = mock(PawnshopRepository.class);
        service = new ProductInspectionServiceImpl(productRepository, inspectionRepository, resellerRepository, pawnshopRepository);
    }

    @Test
    void reviewProduct_accepted() {
        // Arrange
        int productId = 1;
        int resellerId = 10;
        int pawnshopId = 20;
        String comments = "Looks good";

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setApprovalStatus(ApprovalStatusEnum.PENDING);

        ResellerEntity reseller = new ResellerEntity();
        reseller.setId(resellerId);

        PawnshopEntity pawnshop = new PawnshopEntity();
        pawnshop.setId(pawnshopId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(resellerRepository.findById(resellerId)).thenReturn(Optional.of(reseller));
        when(pawnshopRepository.findById(pawnshopId)).thenReturn(Optional.of(pawnshop));

        ProductInspectionRequest request = ProductInspectionRequest.newBuilder()
                .setProductId(productId)
                .setResellerId(resellerId)
                .setPawnshopId(pawnshopId)
                .setIsAccepted(true)
                .setComments(comments)
                .build();

        StreamObserver<ProductInspectionResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.reviewProduct(request, responseObserver);

        // Assert
        ArgumentCaptor<ProductInspectionResponse> responseCaptor = ArgumentCaptor.forClass(ProductInspectionResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProductInspectionResponse response = responseCaptor.getValue();
        assertEquals(productId, response.getProductId());
        // Proto status mapping might differ if enum names match. Assuming enum logic is correct.
        assertEquals(com.respawnmarket.ApprovalStatus.REVIEWING, response.getApprovalStatus());
        assertEquals(pawnshopId, response.getPawnshopId());
        assertEquals(comments, response.getComments());

        assertEquals(ApprovalStatusEnum.REVIEWING, product.getApprovalStatus());
        assertEquals(pawnshop, product.getPawnshop());

        verify(inspectionRepository).save(any(InspectionEntity.class));
        verify(productRepository).save(product);
    }

    @Test
    void reviewProduct_rejected() {
        // Arrange
        int productId = 1;
        int resellerId = 10;
        // pawnshopId ignored if rejected, but let's provide 0
        String comments = "Bad condition";

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setApprovalStatus(ApprovalStatusEnum.PENDING);

        ResellerEntity reseller = new ResellerEntity();
        reseller.setId(resellerId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(resellerRepository.findById(resellerId)).thenReturn(Optional.of(reseller));

        ProductInspectionRequest request = ProductInspectionRequest.newBuilder()
                .setProductId(productId)
                .setResellerId(resellerId)
                .setIsAccepted(false)
                .setComments(comments)
                .build();

        StreamObserver<ProductInspectionResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.reviewProduct(request, responseObserver);

        // Assert
        ArgumentCaptor<ProductInspectionResponse> responseCaptor = ArgumentCaptor.forClass(ProductInspectionResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProductInspectionResponse response = responseCaptor.getValue();
        assertEquals(com.respawnmarket.ApprovalStatus.REJECTED, response.getApprovalStatus());
        assertEquals(0, response.getPawnshopId());

        assertEquals(ApprovalStatusEnum.REJECTED, product.getApprovalStatus());
        assertNull(product.getPawnshop());
    }

    @Test
    void reviewProduct_rejected_noComments_throws() {
        // Arrange
        int productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(new ProductEntity()));
        when(resellerRepository.findById(anyInt())).thenReturn(Optional.of(new ResellerEntity()));

        ProductInspectionRequest request = ProductInspectionRequest.newBuilder()
                .setProductId(productId)
                .setIsAccepted(false)
                .setComments("") // Empty comments
                .build();

        StreamObserver<ProductInspectionResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.reviewProduct(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertTrue(error instanceof StatusRuntimeException);
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
        assertEquals("Comments must be provided when rejecting a product", ((StatusRuntimeException) error).getStatus().getDescription());
    }

    @Test
    void verifyProduct_approved() {
        // Arrange
        int productId = 1;
        int resellerId = 10;
        String comments = "Verified OK";

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        // Usually should be REVIEWING before verifying
        product.setApprovalStatus(ApprovalStatusEnum.REVIEWING);

        ResellerEntity reseller = new ResellerEntity();
        reseller.setId(resellerId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(resellerRepository.findById(resellerId)).thenReturn(Optional.of(reseller));

        ProductVerificationRequest request = ProductVerificationRequest.newBuilder()
                .setProductId(productId)
                .setResellerId(resellerId)
                .setIsAccepted(true)
                .setComments(comments)
                .build();

        StreamObserver<ProductVerificationResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.verifyProduct(request, responseObserver);

        // Assert
        ArgumentCaptor<ProductVerificationResponse> responseCaptor = ArgumentCaptor.forClass(ProductVerificationResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProductVerificationResponse response = responseCaptor.getValue();
        assertEquals(com.respawnmarket.ApprovalStatus.APPROVED, response.getApprovalStatus());
        assertEquals(ApprovalStatusEnum.APPROVED, product.getApprovalStatus());

        verify(inspectionRepository).save(any(InspectionEntity.class));
        verify(productRepository).save(product);
    }
}

