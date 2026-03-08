package com.respawn.services;

import com.respawn.entities.*;
import com.respawn.entities.enums.ApprovalStatusEnum;
import com.respawn.entities.enums.CategoryEnum;
import com.respawn.repositories.*;
import com.respawnmarket.BuyProductsRequest;
import com.respawnmarket.BuyProductsResponse;
import com.respawnmarket.CartItem;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PurchaseServiceImplTest {

    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private ShoppingCartRepository shoppingCartRepository;
    private CartProductRepository cartProductRepository;
    private TransactionRepository transactionRepository;
    private PurchaseServiceImpl service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        productRepository = mock(ProductRepository.class);
        shoppingCartRepository = mock(ShoppingCartRepository.class);
        cartProductRepository = mock(CartProductRepository.class);
        transactionRepository = mock(TransactionRepository.class);

        service = new PurchaseServiceImpl(
                customerRepository,
                productRepository,
                shoppingCartRepository,
                cartProductRepository,
                transactionRepository
        );
    }

    @Test
    void buyProducts_success() {
        // Arrange
        int customerId = 1;
        int productId = 100;
        double price = 50.0;
        int quantity = 2;

        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setPrice(price);
        product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
        product.setCategory(CategoryEnum.ELECTRONICS);
        product.setSeller(new CustomerEntity()); // Mock seller
        product.setRegisterDate(LocalDateTime.now());
        product.setCondition("Good");
        product.setName("IPhone 7");
        product.setDescription("Old but gold");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ShoppingCartEntity savedCart = mock(ShoppingCartEntity.class);
        when(savedCart.getId()).thenReturn(10);
        when(savedCart.getTotalPrice()).thenReturn(price * quantity);
        when(shoppingCartRepository.save(any(ShoppingCartEntity.class))).thenReturn(savedCart);
        when(cartProductRepository.save(any(CartProductEntity.class))).thenReturn(new CartProductEntity());

        TransactionEntity savedTx = mock(TransactionEntity.class);
        when(savedTx.getId()).thenReturn(999);
        when(savedTx.getDate()).thenReturn(LocalDateTime.now());
        // when(savedTx.getShoppingCart()).thenReturn(savedCart); // Removed
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(savedTx);

        BuyProductsRequest request = BuyProductsRequest.newBuilder()
                .setCustomerId(customerId)
                .addItems(CartItem.newBuilder()
                        .setProductId(productId)
                        .setQuantity(quantity)
                        .build())
                .build();

        StreamObserver<BuyProductsResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.buyProducts(request, responseObserver);

        // Assert
        ArgumentCaptor<BuyProductsResponse> responseCaptor = ArgumentCaptor.forClass(BuyProductsResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        BuyProductsResponse response = responseCaptor.getValue();
        assertEquals(999, response.getTransaction().getId());
        assertEquals(10, response.getShoppingCart().getId());
        assertEquals(100.0, response.getShoppingCart().getTotalPrice()); // 50 * 2

        assertEquals(1, response.getCartProductsCount());
        assertEquals(productId, response.getCartProducts(0).getProductId());
        assertEquals(quantity, response.getCartProducts(0).getQuantity());

        assertTrue(product.isSold());
        verify(productRepository, times(1)).save(product); // setSold(true)
    }

    @Test
    void buyProducts_customerNotFound() {
        // Arrange
        int customerId = 999;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        BuyProductsRequest request = BuyProductsRequest.newBuilder()
                .setCustomerId(customerId)
                .build();
        StreamObserver<BuyProductsResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.buyProducts(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertInstanceOf(StatusRuntimeException.class, error);
        assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) error).getStatus().getCode());

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void buyProducts_productNotApproved() {
        // Arrange
        int productId = 101;
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setApprovalStatus(ApprovalStatusEnum.PENDING); // Not APPROVED

        when(customerRepository.findById(1)).thenReturn(Optional.of(new CustomerEntity()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        BuyProductsRequest request = BuyProductsRequest.newBuilder()
                .setCustomerId(1)
                .addItems(CartItem.newBuilder()
                        .setProductId(productId)
                        .setQuantity(1)
                        .build())
                .build();

        StreamObserver<BuyProductsResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.buyProducts(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertTrue(error instanceof StatusRuntimeException);
        assertEquals(Status.FAILED_PRECONDITION.getCode(), ((StatusRuntimeException) error).getStatus().getCode());

        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void buyProducts_productAlreadySold() {
        // Arrange
        int productId = 102;
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setApprovalStatus(ApprovalStatusEnum.APPROVED);
        product.setSold(true); // Already sold

        when(customerRepository.findById(1)).thenReturn(Optional.of(new CustomerEntity()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        BuyProductsRequest request = BuyProductsRequest.newBuilder()
                .setCustomerId(1)
                .addItems(CartItem.newBuilder()
                        .setProductId(productId)
                        .setQuantity(1)
                        .build())
                .build();

        StreamObserver<BuyProductsResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.buyProducts(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertEquals(Status.FAILED_PRECONDITION.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
    }
}



