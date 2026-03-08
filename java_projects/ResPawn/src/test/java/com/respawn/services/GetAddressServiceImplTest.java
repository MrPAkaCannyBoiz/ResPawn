package com.respawn.services;

import com.respawn.dtos.PawnshopAddressPostalDto;
import com.respawn.repositories.AddressRepository;
import com.respawn.repositories.PawnshopRepository;
import com.respawnmarket.GetAllPawnshopAddressesRequest;
import com.respawnmarket.GetAllPawnshopAddressesResponse;
import com.respawnmarket.PawnshopAddressWithPostal;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAddressServiceImplTest {

    private AddressRepository addressRepository;
    private PawnshopRepository pawnshopRepository;
    private GetAddressServiceImpl service;

    @BeforeEach
    void setUp() {
        addressRepository = mock(AddressRepository.class);
        pawnshopRepository = mock(PawnshopRepository.class);
        service = new GetAddressServiceImpl(addressRepository, pawnshopRepository);
    }

    @Test
    void getAllPawnshopAddresses_success() {
        // Arrange
        PawnshopAddressPostalDto dto = new PawnshopAddressPostalDto(
                1, "123 Main St", "Unit 1", 12345, "London", 101); // Assuming constructor or setters

        List<PawnshopAddressPostalDto> dtoList = Collections.singletonList(dto);
        when(addressRepository.getAllPawnshopAddresses()).thenReturn(dtoList);

        GetAllPawnshopAddressesRequest request = GetAllPawnshopAddressesRequest.newBuilder().build();
        StreamObserver<GetAllPawnshopAddressesResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.getAllPawnshopAddresses(request, responseObserver);

        // Assert
        ArgumentCaptor<GetAllPawnshopAddressesResponse> responseCaptor = ArgumentCaptor.forClass(GetAllPawnshopAddressesResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetAllPawnshopAddressesResponse response = responseCaptor.getValue();
        assertEquals(1, response.getAddressesCount());
        PawnshopAddressWithPostal result = response.getAddressesList().get(0);

        assertEquals(1, result.getAddress().getId());
        assertEquals("123 Main St", result.getAddress().getStreetName());
        assertEquals("Unit 1", result.getAddress().getSecondaryUnit());
        assertEquals(12345, result.getAddress().getPostalCode());
        assertEquals(12345, result.getPostal().getPostalCode());
        assertEquals("London", result.getPostal().getCity());
        assertEquals(101, result.getPawnshopId());
    }

    @Test
    void getAllPawnshopAddresses_notFound() {
        // Arrange
        when(addressRepository.getAllPawnshopAddresses()).thenReturn(Collections.emptyList());

        GetAllPawnshopAddressesRequest request = GetAllPawnshopAddressesRequest.newBuilder().build();
        StreamObserver<GetAllPawnshopAddressesResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.getAllPawnshopAddresses(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());

        // Check error status code? The service implementation calls responseObserver.onError(...) with StatusRuntimeException
        // Actually it calls `Status.NOT_FOUND...asRuntimeException()`.

        Throwable error = errorCaptor.getValue();
        // Since we are not using assertThrows here because method catches nothing but calls onError
        // But the method is void.
        // wait, the implementation is:
        /*
        if (pawnshopAddressPostalDtos.isEmpty())
        {
            responseObserver.onError(Status.NOT_FOUND...);
            return;
        }
        */

        // So we need to check if onError was called with correct status.
        // It's a StatusRuntimeException (or similar)
        // We can inspect the error.

        assertTrue(error instanceof io.grpc.StatusRuntimeException);
        assertEquals(Status.NOT_FOUND.getCode(), ((io.grpc.StatusRuntimeException) error).getStatus().getCode());

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
}



