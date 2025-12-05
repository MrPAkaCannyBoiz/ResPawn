package org.example.respawnmarket.Service;

import com.respawnmarket.CustomerLoginRequest;
import com.respawnmarket.CustomerLoginResponse;
import com.respawnmarket.CustomerLoginServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.CustomerEntity;
import org.example.respawnmarket.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class CustomerLoginServiceImpl extends CustomerLoginServiceGrpc.CustomerLoginServiceImplBase
{
    private CustomerRepository customerRepository;

    @Autowired
    public CustomerLoginServiceImpl(CustomerRepository customerRepository)
    {
        this.customerRepository = customerRepository;
    }

    @Override
    public void login(CustomerLoginRequest request, StreamObserver<CustomerLoginResponse> responseObserver)
    {
        String email = request.getEmail();
        String password = request.getPassword();

        CustomerEntity loginCustomer = customerRepository.findByEmail(email);
        if (loginCustomer == null)
        {
          throwNotFoundIfInvalidCredentials(responseObserver);
          return;
        }
        boolean isPasswordMatch = BCrypt.checkpw(password, loginCustomer.getPassword());
        if (!isPasswordMatch)
        {
          throwNotFoundIfInvalidCredentials(responseObserver);
          return;
        }
        CustomerLoginResponse response = CustomerLoginResponse.newBuilder()
                .setCustomerId(loginCustomer.getId())
                .setFirstName(loginCustomer.getFirstName())
                .setLastName(loginCustomer.getLastName())
                .setEmail(loginCustomer.getEmail())
                .setPhoneNumber(loginCustomer.getPhoneNumber())
                .setCanSell(loginCustomer.isCanSell())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void throwNotFoundIfInvalidCredentials
        (StreamObserver<CustomerLoginResponse> responseObserver)
    {
      responseObserver.onError(Status.NOT_FOUND
          .withDescription("Invalid email or password") // show vague error message for security
          .asRuntimeException());
    }
}
