package org.example.respawnmarket.Service;

import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.CustomerEntity;
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

  @Autowired public GetCustomerServiceImpl(
      CustomerRepository customerRepository,
      AddressRepository addressRepository, PostalRepository postalRepository)
  {
    this.customerRepository = customerRepository;
    this.addressRepository = addressRepository;
    this.postalRepository = postalRepository;
  }

    // use case for getting customer info (admin only)
    @Override
    public void getCustomer(GetCustomerRequest request,
                                StreamObserver<GetCustomerResponse> responseObserver)
    {
        CustomerEntity givenCustomer = customerRepository.findById(request.getCustomerId())
                .orElse(null);
        throwGrpcNotFoundIfNull(givenCustomer, responseObserver);
        assert givenCustomer != null;

        List<Address> addresses = getAddressesForCustomer(request.getCustomerId());
        List<Postal> postals = getPostalsForCustomer(request.getCustomerId());
        NonSensitiveCustomerInfo customerDto = NonSensitiveCustomerInfo.newBuilder()
                .setId(givenCustomer.getId())
                .setFirstName(givenCustomer.getFirstName())
                .setLastName(givenCustomer.getLastName())
                .setEmail(givenCustomer.getEmail())
                .setPhoneNumber(givenCustomer.getPhoneNumber())
                .addAllAddresses(addresses)
                .addAllPostals(postals)
                .setCanSell(givenCustomer.isCanSell())
                .build();

        GetCustomerResponse response = GetCustomerResponse.newBuilder().setCustomer(customerDto).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllCustomers(GetAllCustomersRequest request,
                                StreamObserver<GetAllCustomersResponse> responseObserver)
    {
        List<CustomerEntity> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("No customers found")
                            .asRuntimeException()
            );
            return;
        }
        List<NonSensitiveCustomerInfo> customerDtos = customerRepository.findAll()
                .stream()
                .map(customerEntity -> NonSensitiveCustomerInfo.newBuilder()
                        .setId(customerEntity.getId())
                        .setFirstName(customerEntity.getFirstName())
                        .setLastName(customerEntity.getLastName())
                        .setEmail(customerEntity.getEmail())
                        .setPhoneNumber(customerEntity.getPhoneNumber())
                        .setCanSell(customerEntity.isCanSell())
                        .addAllAddresses(getAddressesForCustomer(customerEntity.getId()))
                        .addAllPostals(getPostalsForCustomer(customerEntity.getId()))
                        .build())
                .toList();

        GetAllCustomersResponse response = GetAllCustomersResponse.newBuilder()
                .addAllCustomers(customerDtos)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Address> getAddressesForCustomer(int customerId)
    {
        return addressRepository.findAddressByCustomerId(customerId)
                .stream()
                .map(addressEntity -> Address.newBuilder()
                        .setId(addressEntity.getId())
                        .setStreetName(addressEntity.getStreetName())
                        .setSecondaryUnit(addressEntity.getSecondaryUnit())
                        .setPostalCode(addressEntity.getPostal().getPostalCode())
                        .build())
                .toList();
    }

    private List<Postal> getPostalsForCustomer(int customerId)
    {
        return postalRepository.findByCustomerId(customerId)
                .stream()
                .map(postalEntity -> Postal.newBuilder()
                        .setPostalCode(postalEntity.getPostalCode())
                        .setCity(postalEntity.getCity())
                        .build())
                .toList();
    }

    private void throwGrpcNotFoundIfNull(CustomerEntity customerEntity, StreamObserver<?> responseObserver)
    {
        if (customerEntity == null)
        {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("Customer not found")
                            .asRuntimeException()
            );
        }
    }
}
