package org.example.respawnmarket.Service;

import com.respawnmarket.*;
import org.example.respawnmarket.dtos.CustomerDto;
import org.example.respawnmarket.repositories.AddressRepository;
import org.example.respawnmarket.repositories.CustomerAddressRepository;
import org.example.respawnmarket.repositories.CustomerRepository;
import org.example.respawnmarket.repositories.PostalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GetCustomerServiceImpl extends GetCustomerServiceGrpc.GetCustomerServiceImplBase
{
    private CustomerRepository customerRepository;
    private AddressRepository addressRepository;
    private PostalRepository postalRepository;
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    public GetCustomerServiceImpl(CustomerRepository customerRepository, AddressRepository addressRepository, PostalRepository postalRepository)
    {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.postalRepository = postalRepository;
    }

    // use case for getting customer info (admin only)
    public void getCustomer(GetCustomerRequest request,
                                io.grpc.stub.StreamObserver<GetCustomerResponse> responseObserver)
    {
        var givenCustomer = customerRepository.findById(request.getCustomerId()).orElse(null);
        assert givenCustomer != null;
        Customer customerDto = Customer.newBuilder()
                .setId(givenCustomer.getId())
                .setFirstName(givenCustomer.getFirstName())
                .setLastName(givenCustomer.getLastName())
                .setEmail(givenCustomer.getEmail())
                .setPhoneNumber(givenCustomer.getPhoneNumber())
                .build();

        List<Address> addresses = addressRepository.findAddressByCustomerId(request.getCustomerId())
                .stream()
                .map(addressEntity -> Address.newBuilder()
                        .setId(addressEntity.getId())
                        .setStreetName(addressEntity.getStreetName())
                        .setSecondaryUnit(addressEntity.getSecondaryUnit())
                        .setPostalCode(addressEntity.getPostal().getPostalCode())
                        .build())
                .toList();
        
        List<Postal> postals = postalRepository.findByCustomerId(request.getCustomerId())
                .stream()
                .map(postalEntity -> Postal.newBuilder()
                        .setPostalCode(postalEntity.getPostalCode())
                        .setCity(postalEntity.getCity())
                        .build())
                .toList();

        GetCustomerResponse response = GetCustomerResponse.newBuilder()
                .setCustomer(customerDto)
                .addAllAddresses(addresses)
                .addAllPostals(postals)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
