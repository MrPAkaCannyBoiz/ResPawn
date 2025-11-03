package org.example.respawnmarket.Service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;
import com.respawnmarket.CustomerRegisterServiceGrpc;
import com.respawnmarket.RegisterCustomerResponse;

@Service
public class RegisterCustomerServiceImplClient
{
    private CustomerRegisterServiceGrpc.CustomerRegisterServiceBlockingStub blockingStub;

    public RegisterCustomerServiceImplClient()
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6767)
                .usePlaintext()
                .build();
        this.blockingStub = CustomerRegisterServiceGrpc.newBlockingStub(channel);
    }

    public void registerCustomer(String firstName, String lastName, String email, String password
            , String phoneNumber, String streetName, String secondaryUnit, int postalCode ,String city)
    {
        com.respawnmarket.RegisterCustomerRequest request = com.respawnmarket.RegisterCustomerRequest.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPassword(password)
                .setPhoneNumber(phoneNumber)
                .setStreetName(streetName)
                .setSecondaryUnit(secondaryUnit)
                .setPostalCode(postalCode)
                .setCity(city)
                .build();

        RegisterCustomerResponse response = this.blockingStub.registerCustomer(request);
        System.out.println("Customer registered with ID: " + response.getCustomer().getId());
    }
}
