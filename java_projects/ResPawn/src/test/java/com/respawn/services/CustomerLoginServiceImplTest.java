package com.respawn.services;

import com.respawn.entities.CustomerEntity;
import com.respawn.repositories.CustomerRepository;
import com.respawnmarket.CustomerLoginRequest;
import com.respawnmarket.CustomerLoginResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerLoginServiceImplTest {

    private CustomerRepository customerRepository;
    private CustomerLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        service = new CustomerLoginServiceImpl(customerRepository);
    }

    @Test
    void login_success() {
        String email = "john@example.com";
        String password = "password123";
        String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        CustomerEntity customer = new CustomerEntity("John", "Doe", email, encodedPassword, "5551234567");
        customer.setId(1);
        customer.setCanSell(true);

        when(customerRepository.findByEmail(email)).thenReturn(customer);

        CustomerLoginRequest request = CustomerLoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();
        StreamObserver<CustomerLoginResponse> responseObserver = mock(StreamObserver.class);

        service.login(request, responseObserver);

        ArgumentCaptor<CustomerLoginResponse> captor = ArgumentCaptor.forClass(CustomerLoginResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        CustomerLoginResponse response = captor.getValue();
        assertEquals(1, response.getCustomerId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(email, response.getEmail());
        assertEquals("5551234567", response.getPhoneNumber());
        assertTrue(response.getCanSell());
    }

    @Test
    void login_customerNotFound_returnsNotFound() {
        String email = "nonexistent@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(null);

        CustomerLoginRequest request = CustomerLoginRequest.newBuilder()
                .setEmail(email)
                .setPassword("anypassword")
                .build();
        StreamObserver<CustomerLoginResponse> responseObserver = mock(StreamObserver.class);

        service.login(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());

        StatusRuntimeException error = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.NOT_FOUND.getCode(), error.getStatus().getCode());
        assertEquals("Invalid email or password", error.getStatus().getDescription());
    }

    @Test
    void login_wrongPassword_returnsNotFound() {
        String email = "john@example.com";
        String encodedPassword = BCrypt.hashpw("realPassword", BCrypt.gensalt());

        CustomerEntity customer = new CustomerEntity("John", "Doe", email, encodedPassword, "5551234567");
        customer.setId(1);
        when(customerRepository.findByEmail(email)).thenReturn(customer);

        CustomerLoginRequest request = CustomerLoginRequest.newBuilder()
                .setEmail(email)
                .setPassword("wrongPassword")
                .build();
        StreamObserver<CustomerLoginResponse> responseObserver = mock(StreamObserver.class);

        service.login(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());

        StatusRuntimeException error = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.NOT_FOUND.getCode(), error.getStatus().getCode());
        assertEquals("Invalid email or password", error.getStatus().getDescription());
    }
}
