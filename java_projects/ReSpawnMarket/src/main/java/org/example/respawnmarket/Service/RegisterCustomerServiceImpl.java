package org.example.respawnmarket.Service;
import com.respawnmarket.CustomerRegisterServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import org.example.respawnmarket.entities.AddressEntity;
import org.example.respawnmarket.entities.CustomerAddressEntity;
import org.example.respawnmarket.entities.CustomerEntity;
import org.example.respawnmarket.entities.PostalEntity;
import org.example.respawnmarket.repositories.AddressRepository;
import org.example.respawnmarket.repositories.CustomerAddressRepository;
import org.example.respawnmarket.repositories.CustomerRepository;
import org.example.respawnmarket.repositories.PostalRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// manually import generated classes
import com.respawnmarket.Customer;
import com.respawnmarket.Address;
import com.respawnmarket.Postal;
import com.respawnmarket.CustomerAddress;
import com.respawnmarket.RegisterCustomerRequest;
import com.respawnmarket.RegisterCustomerResponse;

import static org.example.respawnmarket.Service.ServiceExtensions.CustomerExceptionExtension.mapDataIntegrityViolation;

// TODO: add validation for each database constraint, so web api can catch errors properly
// TODO: handle two addresses per customer
@Service // Spring Boot will auto-detect this class as a service component
public class RegisterCustomerServiceImpl extends CustomerRegisterServiceGrpc.CustomerRegisterServiceImplBase
{
    private AddressRepository addressRepository;
    private CustomerRepository customerRepository;
    private PostalRepository postalRepository;
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    public RegisterCustomerServiceImpl(AddressRepository addressRepository, CustomerRepository customerRepository, PostalRepository postalRepository, CustomerAddressRepository customerAddressRepository)
    {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
        this.postalRepository = postalRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    @Override
    @Transactional
    public void registerCustomer(RegisterCustomerRequest request,
                                 StreamObserver<RegisterCustomerResponse> responseObserver)
    {
        // make entity for DB
        PostalEntity givenPostal = new PostalEntity(); // Create an empty PostalEntity to hold postal info
        // 2 scenarios: postal code exists or not
        if (!postalRepository.existsById(request.getPostalCode()))
        {
            PostalEntity newPostal = new PostalEntity(request.getPostalCode(), request.getCity());
            postalRepository.save(newPostal);
            givenPostal.setPostalCode(newPostal.getPostalCode());
            givenPostal.setCity(newPostal.getCity());
        }
        else
        {
            PostalEntity existingPostal = postalRepository.findById(request.getPostalCode()).orElse(null);
            assert existingPostal != null;
            postalRepository.save(existingPostal);
            givenPostal.setPostalCode(existingPostal.getPostalCode());
            givenPostal.setCity(existingPostal.getCity());
        }
        try
        {
            // encrypt password before saving to DB (omitted for brevity)
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            // create and save customer, address, and customerAddress entities
            CustomerEntity customer = new CustomerEntity(request.getFirstName(), request.getLastName()
                    , request.getEmail(), encodedPassword, request.getPhoneNumber());
            if (request.getFirstName().isEmpty() || request.getLastName().isEmpty()
                    || request.getEmail().isEmpty() || request.getPassword().isEmpty()
                    || request.getPhoneNumber().isEmpty()
                    || request.getStreetName().isEmpty()
                    || request.getCity().isEmpty())
            {
               responseObserver.onError(Status.INVALID_ARGUMENT
                       .withDescription("All fields must be filled")
                       .asRuntimeException());
            }
            CustomerEntity savedCustomer = customerRepository.save(customer);
            AddressEntity address = new AddressEntity(request.getStreetName()
                    , request.getSecondaryUnit(), givenPostal);
            AddressEntity savedAddress = addressRepository.save(address);
            CustomerAddressEntity savedCustomerAddress = new CustomerAddressEntity(savedCustomer, savedAddress);
            customerAddressRepository.save(savedCustomerAddress);
            customerRepository.flush();
            addressRepository.flush();
            customerAddressRepository.flush();
            // make dto for response
            System.out.println("Registering customer: " + request);

            Customer customerDto = Customer.newBuilder()
                    .setId(savedCustomer.getId())
                    .setFirstName(savedCustomer.getFirstName())
                    .setLastName(savedCustomer.getLastName())
                    .setEmail(savedCustomer.getEmail())
                    .setPhoneNumber(savedCustomer.getPhoneNumber())
                    .build();

            Address addressDto = Address.newBuilder()
                    .setId(savedAddress.getId())
                    .setStreetName(savedAddress.getStreetName())
                    .setSecondaryUnit(savedAddress.getSecondaryUnit())
                    .setPostalCode(savedAddress.getPostal().getPostalCode())
                    .build();

            Postal postalDto = Postal.newBuilder()
                    .setPostalCode(givenPostal.getPostalCode())
                    .setCity(givenPostal.getCity())
                    .build();

            RegisterCustomerResponse response = RegisterCustomerResponse.newBuilder()
                    .setCustomer(customerDto)
                    .setAddress(addressDto)
                    .setPostal(postalDto)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DataIntegrityViolationException ex) // handle unique constraint violations
        {
            StatusRuntimeException statusEx = mapDataIntegrityViolation(ex);
            responseObserver.onError(Status
                    .INVALID_ARGUMENT
                    .withDescription(statusEx.getMessage()).asRuntimeException());
        }
        catch (Exception ex)
        {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Unexpected error while registering customer")
                            .withCause(ex)
                            .asRuntimeException()
            );
        }
    }






}
