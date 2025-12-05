package org.example.respawnmarket.Service;

import com.respawnmarket.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.ResellerEntity;
import org.example.respawnmarket.repositories.ResellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class ResellerLoginServiceImpl extends ResellerLoginServiceGrpc.ResellerLoginServiceImplBase
{
    private ResellerRepository resellerRepository;

    @Autowired
    public ResellerLoginServiceImpl(ResellerRepository resellerRepository)
    {
        this.resellerRepository = resellerRepository;
    }

    @Override
    public void login(
            ResellerLoginRequest request, StreamObserver<ResellerLoginResponse> responseObserver)
    {
        String usernarme = request.getUsername();
        String password = request.getPassword();

        ResellerEntity loginReseller = resellerRepository.findByUsername(usernarme);
        if (loginReseller == null)
        {
            throwNotFoundIfInvalidCredentials(responseObserver);
            return;
        }
        boolean isPasswordMatch = BCrypt.checkpw(password, loginReseller.getPassword());

        if (!isPasswordMatch)
        {
            throwNotFoundIfInvalidCredentials(responseObserver);
            return;
        }
        ResellerLoginResponse response = ResellerLoginResponse.newBuilder()
                .setId(loginReseller.getId())
                .setUsername(loginReseller.getUsername())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void throwNotFoundIfInvalidCredentials
            (StreamObserver<ResellerLoginResponse> responseObserver)
    {
        responseObserver.onError(Status.NOT_FOUND
                .withDescription("Invalid email or password") // show vague error message for security
                .asRuntimeException());
    }
}