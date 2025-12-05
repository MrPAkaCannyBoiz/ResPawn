package org.example.respawnmarket.Service;

import com.respawnmarket.CustomerInspectionServiceGrpc;
import com.respawnmarket.EnableSellingRequest;
import com.respawnmarket.EnableSellingResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import org.example.respawnmarket.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerInspectionServiceImpl extends CustomerInspectionServiceGrpc.CustomerInspectionServiceImplBase
{
    private CustomerRepository customerRepository;

    @Autowired
    public CustomerInspectionServiceImpl(CustomerRepository customerRepository)
    {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public void setCanSell(EnableSellingRequest request,
                           StreamObserver<EnableSellingResponse> responseObserver)
    {
        int customerId = request.getCustomerId();
        boolean canSell = request.getCanSell();

        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Customer not found with ID: " + customerId)
                        .asRuntimeException());
        customer.setCanSell(canSell);
        customerRepository.save(customer);
        customerRepository.flush();

        EnableSellingResponse response = EnableSellingResponse.newBuilder()
                .setCustomerId(customerId)
                .setCanSell(canSell)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
