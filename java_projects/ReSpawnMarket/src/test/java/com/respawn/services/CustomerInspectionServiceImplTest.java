package com.respawn.services;

import com.respawn.entities.CustomerEntity;
import com.respawn.repositories.CustomerRepository;
import com.respawnmarket.EnableSellingRequest;
import com.respawnmarket.EnableSellingResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class CustomerInspectionServiceImplTest {

    private CustomerRepository customerRepository;
    private CustomerInspectionServiceImpl service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        service = new CustomerInspectionServiceImpl(customerRepository);
    }

    @Test
    void setCanSell_success() {
        // Arrange
        int customerId = 1;
        boolean canSell = true;
        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);
        customer.setCanSell(false);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        EnableSellingRequest request = EnableSellingRequest.newBuilder()
                .setCustomerId(customerId)
                .setCanSell(canSell)
                .build();

        StreamObserver<EnableSellingResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.setCanSell(request, responseObserver);

        // Assert
        ArgumentCaptor<EnableSellingResponse> responseCaptor = ArgumentCaptor.forClass(EnableSellingResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        EnableSellingResponse response = responseCaptor.getValue();
        assertEquals(customerId, response.getCustomerId());
        assertEquals(canSell, response.getCanSell());
        assertTrue(customer.isCanSell());

        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
        verify(customerRepository).flush();
    }

    @Test
    void setCanSell_customerNotFound() {
        // Arrange
        int customerId = 999;
        boolean canSell = true;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        EnableSellingRequest request = EnableSellingRequest.newBuilder()
                .setCustomerId(customerId)
                .setCanSell(canSell)
                .build();

        StreamObserver<EnableSellingResponse> responseObserver = mock(StreamObserver.class);

        // Act & Assert
        // The service does not throw exception directly but calls responseObserver.onError?
        // Wait, the code says:
        // .orElseThrow(() -> Status.NOT_FOUND...asRuntimeException());
        // So it throws RuntimeException. Since it is gRPC implementation, typically we expect it to bubble up or be caught?
        // The service method implementation does not wrap it in try-catch. So it will throw.

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            service.setCanSell(request, responseObserver);
        });

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
}

