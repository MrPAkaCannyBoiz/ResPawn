package com.respawn.services;

import com.respawn.entities.AddressEntity;
import com.respawn.entities.CustomerEntity;
import com.respawn.entities.PostalEntity;
import com.respawn.repositories.AddressRepository;
import com.respawn.repositories.CustomerAddressRepository;
import com.respawn.repositories.CustomerRepository;
import com.respawn.repositories.PostalRepository;
import com.respawnmarket.UpdateCustomerRequest;
import com.respawnmarket.UpdateCustomerResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateCustomerServiceImplTest {

    private AddressRepository addressRepository;
    private CustomerRepository customerRepository;
    private PostalRepository postalRepository;
    private CustomerAddressRepository customerAddressRepository;
    private UpdateCustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        addressRepository = mock(AddressRepository.class);
        customerRepository = mock(CustomerRepository.class);
        postalRepository = mock(PostalRepository.class);
        customerAddressRepository = mock(CustomerAddressRepository.class);
        service = new UpdateCustomerServiceImpl(
                addressRepository,
                customerRepository,
                postalRepository,
                customerAddressRepository
        );
    }

    @Test
    void updateCustomer_success() {
        // Arrange
        int customerId = 1;

        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);
        customer.setFirstName("OldName");
        customer.setLastName("OldLast");
        customer.setEmail("old@example.com");
        customer.setPhoneNumber("000");

        PostalEntity oldPostal = new PostalEntity(11111, "City1");
        AddressEntity address = new AddressEntity("Old St", "Unit 1", oldPostal);
        address.setId(10);

        PostalEntity newPostal = new PostalEntity(22222, "City2");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        List<AddressEntity> addressList = new ArrayList<>();
        addressList.add(address);
        when(addressRepository.findAddressByCustomerId(customerId)).thenReturn(addressList);

        List<PostalEntity> postalList = new ArrayList<>();
        postalList.add(oldPostal);
        when(postalRepository.findByCustomerId(customerId)).thenReturn(postalList);

        when(postalRepository.findById(22222)).thenReturn(Optional.empty()); // New postal not exists
        when(postalRepository.save(any(PostalEntity.class))).thenReturn(newPostal);

        UpdateCustomerRequest request = UpdateCustomerRequest.newBuilder()
                .setCustomerId(customerId)
                .setFirstName("NewName")
                .setLastName("") // No change
                .setEmail("new@example.com")
                .setPhoneNumber("") // No change
                .setStreetName("New St")
                .setSecondaryUnit("") // No change logic? implementation says: if secondary blank, set to secondary. Wait.
                /*
                if (!request.getSecondaryUnit().isBlank()) { updatedAddress.setSecondaryUnit(request.getSecondaryUnit()); }
                else if (request.getStreetName().isBlank()) { updatedAddress.setSecondaryUnit(""); }
                Wait, the else if logic in code:
                if (!request.getSecondaryUnit().isBlank()) ...
                else if (request.getStreetName().isBlank()) ...
                If street name is provided (not blank), and secondary is blank, does nothing to secondary?
                Wait, if street name is provided, "request.getStreetName().isBlank()" is false.
                So if sec is blank, and street is NOT blank, secondary unit remains old?

                Let's check code again:
                if (!request.getStreetName().isBlank()) { updatedAddress.setStreetName(request.getStreetName()); }

                if (!request.getSecondaryUnit().isBlank())
                {
                    updatedAddress.setSecondaryUnit(request.getSecondaryUnit());
                }
                else if (request.getStreetName().isBlank())
                {
                    updatedAddress.setSecondaryUnit("");
                }

                I provided streetName="New St" (not blank). secondary="" (blank).
                So secondary block: first if false. else if (street blank?) -> "New St" is not blank. So false.
                So secondary unit is NOT updated. It remains "Unit 1".
                */
                .setPostalCode(22222)
                .setCity("City2")
                .build();

        StreamObserver<UpdateCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.updateCustomer(request, responseObserver);

        // Assert
        ArgumentCaptor<UpdateCustomerResponse> responseCaptor = ArgumentCaptor.forClass(UpdateCustomerResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        UpdateCustomerResponse response = responseCaptor.getValue();
        assertEquals("NewName", response.getFirstName());
        assertEquals("OldLast", response.getLastName());
        assertEquals("new@example.com", response.getEmail());

        assertEquals("New St", address.getStreetName());
        assertEquals("Unit 1", address.getSecondaryUnit()); // Confirmed logc
        assertEquals(newPostal, address.getPostal());

        verify(customerRepository).save(customer);
        verify(addressRepository).save(address);
    }

    @Test
    void updateCustomer_notFound() {
        // Arrange
        int customerId = 999;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        UpdateCustomerRequest request = UpdateCustomerRequest.newBuilder()
                .setCustomerId(customerId)
                .build();

        StreamObserver<UpdateCustomerResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.updateCustomer(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertTrue(error instanceof StatusRuntimeException);
        assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) error).getStatus().getCode());

        verify(customerRepository, never()).save(any());
    }
}


