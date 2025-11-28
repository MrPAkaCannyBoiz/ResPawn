package org.example.respawnmarket.Service;

import com.respawnmarket.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.PostalEntity;
import org.example.respawnmarket.repositories.AddressRepository;
import org.example.respawnmarket.repositories.CustomerAddressRepository;
import org.example.respawnmarket.repositories.CustomerRepository;
import org.example.respawnmarket.repositories.PostalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: add validation for each database constraint, so web api can catch errors properly
// TODO: handle two addresses per customer
@Service
public class UpdateCustomerServiceImpl extends UpdateCustomerServiceGrpc.UpdateCustomerServiceImplBase
{
    private AddressRepository addressRepository;
    private CustomerRepository customerRepository;
    private PostalRepository postalRepository;
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    public UpdateCustomerServiceImpl(AddressRepository addressRepository, CustomerRepository customerRepository
            , PostalRepository postalRepository, CustomerAddressRepository customerAddressRepository)
    {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
        this.postalRepository = postalRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    public void updateCustomer(UpdateCustomerRequest request,
                               StreamObserver<UpdateCustomerResponse> responseObserver)
    {
        // find customer id to update
        var updatedCustomer = customerRepository.findById(request.getCustomerId()).orElse(null);
        assert updatedCustomer != null;
        // update fields of customer while checking for nulls or white spaces
        request.getFirstName();
        if (!request.getFirstName().isBlank())
        {
            updatedCustomer.setFirstName(request.getFirstName());
        }
        if (!request.getLastName().isBlank())
        {
            updatedCustomer.setLastName(request.getLastName());
        }
        if (!request.getEmail().isBlank())
        {
            updatedCustomer.setEmail(request.getEmail());
        }
        if (!request.getPhoneNumber().isBlank())
        {
            updatedCustomer.setPhoneNumber(request.getPhoneNumber());
        }
        if (!request.getPassword().isBlank())
        {
            // encrypt password before saving to DB (omitted for brevity)
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            updatedCustomer.setPassword(encodedPassword);
        }
        // find address to update (for now only one address per customer)
        var updatedAddress = addressRepository.findAddressByCustomerId(request.getCustomerId()).getFirst();
        assert updatedAddress != null;
        if (!request.getStreetName().isBlank())
        {
            updatedAddress.setStreetName(request.getStreetName());
        }
        if (!request.getSecondaryUnit().isBlank())
        {
            updatedAddress.setSecondaryUnit(request.getSecondaryUnit());
        }
        else if (request.getStreetName().isBlank())
        {
            updatedAddress.setSecondaryUnit("");
        }
        // find postal to update (for now only one postal per customer)
        var updatedPostal = postalRepository.findByCustomerId(request.getCustomerId()).getFirst();
        assert updatedPostal != null;
        // if postal code changed, need to check if new postal exists in DB
        var updatedPostalCode = request.getPostalCode();
        var existingPostal = postalRepository.findById(updatedPostalCode).orElse(null);
        if (existingPostal != null)
        {
            // postal exists, just update address to point to existing postal
            updatedAddress.setPostal(existingPostal);
        }
        else
        {
            // postal does not exist, create new postal and save to DB
            var newPostal = postalRepository
                    .save(new PostalEntity(updatedPostalCode, request.getCity()));
            updatedAddress.setPostal(newPostal);
        }
        // check if constraints is violated before saving for customer
        try
        {
            customerRepository.save(updatedCustomer);
        }
        catch (DataIntegrityViolationException e)
        {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("this email already exists in the system")
                    .asRuntimeException());
            return;
        }
        // the rest ain't have unique constraints, just save
        addressRepository.save(updatedAddress);
        postalRepository.save(updatedPostal);
        // make response dto
        Customer customerDto = Customer.newBuilder()
                .setId(updatedCustomer.getId())
                .setFirstName(updatedCustomer.getFirstName())
                .setLastName(updatedCustomer.getLastName())
                .setEmail(updatedCustomer.getEmail())
                .setPhoneNumber(updatedCustomer.getPhoneNumber())
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

        UpdateCustomerResponse response = UpdateCustomerResponse.newBuilder()
                .setCustomer(customerDto)
                .addAllAddresses(addresses)
                .addAllPostals(postals)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
