package com.respawn.services;

import com.respawn.entities.AddressEntity;
import com.respawn.entities.CustomerAddressEntity;
import com.respawn.entities.CustomerEntity;
import com.respawn.entities.PostalEntity;
import com.respawn.repositories.AddressRepository;
import com.respawn.repositories.CustomerAddressRepository;
import com.respawn.repositories.CustomerRepository;
import com.respawn.repositories.PostalRepository;
import com.respawn.services.kafka.producer.services.EmailProducerImpl;
import com.respawnmarket.RegisterCustomerRequest;
import com.respawnmarket.RegisterCustomerResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterCustomerServiceImplTest {

    private AddressRepository addressRepository;
    private CustomerRepository customerRepository;
    private PostalRepository postalRepository;
    private CustomerAddressRepository customerAddressRepository;
    private EmailProducerImpl emailProducer;
    private RegisterCustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        addressRepository = mock(AddressRepository.class);
        customerRepository = mock(CustomerRepository.class);
        postalRepository = mock(PostalRepository.class);
        customerAddressRepository = mock(CustomerAddressRepository.class);
        emailProducer = mock(EmailProducerImpl.class);

        service = new RegisterCustomerServiceImpl(
                addressRepository,
                customerRepository,
                postalRepository,
                customerAddressRepository,
                emailProducer
        );
    }

    @Test
    void registerCustomer_success() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password123";
        String phoneNumber = "1234567890";
        String street = "123 Main St";
        String unit = "Apt 1";
        int postalCode = 12345;
        String city = "City";

        when(postalRepository.existsById(postalCode)).thenReturn(false);
        // postal save
        when(postalRepository.save(any(PostalEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // customer save
        CustomerEntity savedCustomer = new CustomerEntity(firstName, lastName, email, "encodedPwd", phoneNumber);
        savedCustomer.setId(1);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(savedCustomer);

        // address save
        AddressEntity savedAddress = new AddressEntity(street, unit, new PostalEntity(postalCode, city));
        savedAddress.setId(10);
        when(addressRepository.save(any(AddressEntity.class))).thenReturn(savedAddress);

        RegisterCustomerRequest request = RegisterCustomerRequest.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPassword(password)
                .setPhoneNumber(phoneNumber)
                .setStreetName(street)
                .setSecondaryUnit(unit)
                .setPostalCode(postalCode)
                .setCity(city)
                .build();

        StreamObserver<RegisterCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.registerCustomer(request, responseObserver);

        // Assert
        ArgumentCaptor<RegisterCustomerResponse> responseCaptor = ArgumentCaptor.forClass(RegisterCustomerResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        RegisterCustomerResponse response = responseCaptor.getValue();
        assertEquals(1, response.getCustomer().getId());
        assertEquals(firstName, response.getCustomer().getFirstName());
        assertEquals(email, response.getCustomer().getEmail());
        assertEquals(10, response.getAddress().getId());
        assertEquals(postalCode, response.getPostal().getPostalCode());

        verify(emailProducer).sendWelcomeEmailEvent(email);
        verify(customerAddressRepository).save(any(CustomerAddressEntity.class));
    }

    @Test
    void registerCustomer_shortPassword() {
        // Arrange
        RegisterCustomerRequest request = RegisterCustomerRequest.newBuilder()
                .setPassword("short")
                .setPostalCode(12345) // needed for postal check logic which runs first
                .build();

        when(postalRepository.existsById(12345)).thenReturn(false);
        StreamObserver<RegisterCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.registerCustomer(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
        assertEquals("Password must be at least 8 characters long", ((StatusRuntimeException) error).getStatus().getDescription());

        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerCustomer_emptyFields() {
        // Arrange
        RegisterCustomerRequest request = RegisterCustomerRequest.newBuilder()
                .setPassword("password123")
                .setFirstName("") // Empty
                .setPostalCode(12345)
                .build();

        when(postalRepository.existsById(12345)).thenReturn(false);
        StreamObserver<RegisterCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.registerCustomer(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
        assertEquals("All fields must be filled", ((StatusRuntimeException) error).getStatus().getDescription());
    }
}



