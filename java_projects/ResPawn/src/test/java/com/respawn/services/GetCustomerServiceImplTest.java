package com.respawn.services;

import com.respawn.entities.AddressEntity;
import com.respawn.entities.CustomerEntity;
import com.respawn.entities.PostalEntity;
import com.respawn.repositories.AddressRepository;
import com.respawn.repositories.CustomerRepository;
import com.respawn.repositories.PostalRepository;
import com.respawn.services.kafka.producer.services.EmailProducerImpl;
import com.respawnmarket.GetAllCustomersRequest;
import com.respawnmarket.GetAllCustomersResponse;
import com.respawnmarket.GetCustomerRequest;
import com.respawnmarket.GetCustomerResponse;
import com.respawnmarket.NonSensitiveCustomerInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetCustomerServiceImplTest {

    private CustomerRepository customerRepository;
    private AddressRepository addressRepository;
    private PostalRepository postalRepository;
    private EmailProducerImpl emailProducer;
    private GetCustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        addressRepository = mock(AddressRepository.class);
        postalRepository = mock(PostalRepository.class);
        emailProducer = mock(EmailProducerImpl.class);
        service = new GetCustomerServiceImpl(customerRepository, addressRepository, postalRepository, emailProducer);
    }

    @Test
    void getCustomer_success() {
        // Arrange
        int customerId = 1;
        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setCanSell(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findAddressByCustomerId(customerId)).thenReturn(Collections.emptyList());
        when(postalRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        GetCustomerRequest request = GetCustomerRequest.newBuilder().setCustomerId(customerId).build();
        StreamObserver<GetCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.getCustomer(request, responseObserver);

        // Assert
        ArgumentCaptor<GetCustomerResponse> responseCaptor = ArgumentCaptor.forClass(GetCustomerResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetCustomerResponse response = responseCaptor.getValue();
        NonSensitiveCustomerInfo customerInfo = response.getCustomer();
        assertEquals(customerId, customerInfo.getId());
        assertEquals("John", customerInfo.getFirstName());
        assertEquals("Doe", customerInfo.getLastName());
        assertEquals("john.doe@example.com", customerInfo.getEmail());
        assertEquals("1234567890", customerInfo.getPhoneNumber());
        assertTrue(customerInfo.getCanSell());
    }

    @Test
    void getCustomer_notFound() {
        // Arrange
        int customerId = 999;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        GetCustomerRequest request = GetCustomerRequest.newBuilder().setCustomerId(customerId).build();
        StreamObserver<GetCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        // The implementation calls "throwGrpcNotFoundIfNull" which checks if null, then onError.
        // But `findById` returns Optional. empty -> orElse(null) -> null.
        // Then `throwGrpcNotFoundIfNull` checks if null. If null -> onError.
        // It does NOT throw, it calls onError.

        service.getCustomer(request, responseObserver);

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
    void getAllCustomers_success() {
        // Arrange
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        // FIX: Initialize these fields. Protobuf setters will throw NPE if passed null.
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setCanSell(true);

        List<CustomerEntity> customers = Collections.singletonList(customer);
        when(customerRepository.findAll()).thenReturn(customers);

        // Mock addresses/postals for the stream map
        when(addressRepository.findAddressByCustomerId(1)).thenReturn(Collections.emptyList());
        when(postalRepository.findByCustomerId(1)).thenReturn(Collections.emptyList());

        GetAllCustomersRequest request = GetAllCustomersRequest.newBuilder().build();
        StreamObserver<GetAllCustomersResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.getAllCustomers(request, responseObserver);

        // Assert
        ArgumentCaptor<GetAllCustomersResponse> responseCaptor = ArgumentCaptor.forClass(GetAllCustomersResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetAllCustomersResponse response = responseCaptor.getValue();
        assertEquals(1, response.getCustomersCount());
        assertEquals("John", response.getCustomers(0).getFirstName());
    }

    @Test
    void getAllCustomers_notFound() {
        // Arrange
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        GetAllCustomersRequest request = GetAllCustomersRequest.newBuilder().build();
        StreamObserver<GetAllCustomersResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.getAllCustomers(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertTrue(error instanceof StatusRuntimeException);
        assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) error).getStatus().getCode());

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
}

