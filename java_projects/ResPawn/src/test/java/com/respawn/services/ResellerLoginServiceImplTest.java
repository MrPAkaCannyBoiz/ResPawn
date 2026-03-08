package com.respawn.services;

import com.respawn.entities.ResellerEntity;
import com.respawn.repositories.ResellerRepository;
import com.respawnmarket.ResellerLoginRequest;
import com.respawnmarket.ResellerLoginResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResellerLoginServiceImplTest {

    private ResellerRepository resellerRepository;
    private ResellerLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        resellerRepository = mock(ResellerRepository.class);
        service = new ResellerLoginServiceImpl(resellerRepository);
    }

    @Test
    void login_success() {
        // Arrange
        String username = "reseller1";
        String password = "password123";
        String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        ResellerEntity reseller = new ResellerEntity();
        reseller.setId(1);
        reseller.setUsername(username);
        reseller.setPassword(encodedPassword);

        when(resellerRepository.findByUsername(username)).thenReturn(reseller);

        ResellerLoginRequest request = ResellerLoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        StreamObserver<ResellerLoginResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.login(request, responseObserver);

        // Assert
        ArgumentCaptor<ResellerLoginResponse> responseCaptor = ArgumentCaptor.forClass(ResellerLoginResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ResellerLoginResponse response = responseCaptor.getValue();
        assertEquals(1, response.getId());
        assertEquals(username, response.getUsername());
    }

    @Test
    void login_invalidUsername() {
        // Arrange
        String username = "nonexistent";
        when(resellerRepository.findByUsername(username)).thenReturn(null);

        ResellerLoginRequest request = ResellerLoginRequest.newBuilder()
                .setUsername(username)
                .setPassword("pwd")
                .build();
        StreamObserver<ResellerLoginResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.login(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertTrue(error instanceof StatusRuntimeException);
        assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
        assertEquals("Invalid username or password", ((StatusRuntimeException) error).getStatus().getDescription());
    }

    @Test
    void login_invalidPassword() {
        // Arrange
        String username = "reseller1";
        String password = "wrongPassword";
        String encodedPassword = BCrypt.hashpw("realPassword", BCrypt.gensalt());

        ResellerEntity reseller = new ResellerEntity();
        reseller.setId(1);
        reseller.setUsername(username);
        reseller.setPassword(encodedPassword);

        when(resellerRepository.findByUsername(username)).thenReturn(reseller);

        ResellerLoginRequest request = ResellerLoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
        StreamObserver<ResellerLoginResponse> responseObserver = mock(StreamObserver.class);

        // Act
        service.login(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
        assertEquals("Invalid username or password", ((StatusRuntimeException) error).getStatus().getDescription());
    }
}

