package org.example.respawnmarket.Service;

import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.dtos.PawnshopAddressPostalDto;
import org.example.respawnmarket.repositories.AddressRepository;
import org.example.respawnmarket.repositories.PawnshopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAddressServiceImpl extends GetAddressServiceGrpc.GetAddressServiceImplBase
{
    private AddressRepository addressRepository;
    private PawnshopRepository pawnshopRepository;

    @Autowired
    public GetAddressServiceImpl(AddressRepository addressRepository, PawnshopRepository pawnshopRepository)
    {
        this.addressRepository = addressRepository;
        this.pawnshopRepository = pawnshopRepository;
    }

    @Override
    public void getAllPawnshopAddresses(GetAllPawnshopAddressesRequest request,
                                       StreamObserver<GetAllPawnshopAddressesResponse> responseObserver)
    {
        List<PawnshopAddressPostalDto> pawnshopAddressPostalDtos = addressRepository.getAllPawnshopAddresses();
        if (pawnshopAddressPostalDtos.isEmpty())
        {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("No pawnshop addresses found")
                            .asRuntimeException());
            return;
        }
        List<PawnshopAddressWithPostal> address = pawnshopAddressPostalDtos.stream()
                .map(this::toPawnshopAddressWithPostalProto)
                .toList();

        GetAllPawnshopAddressesResponse response = GetAllPawnshopAddressesResponse.newBuilder()
                .addAllAddresses(address)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private PawnshopAddressWithPostal toPawnshopAddressWithPostalProto(PawnshopAddressPostalDto dto)
    {
        Address addressProto = Address.newBuilder()
                .setId(dto.getAddressId())
                .setStreetName(dto.getStreetName())
                .setSecondaryUnit(dto.getSecondaryUnit())
                .setPostalCode(dto.getPostalCode())
                .build();
        Postal postalProto = Postal.newBuilder()
                .setPostalCode(dto.getPostalCode())
                .setCity(dto.getCity())
                .build();

        return PawnshopAddressWithPostal.newBuilder()
                .setAddress(addressProto)
                .setPostal(postalProto)
                .setPawnshopId(dto.getPawnshopId())
                .build();
    }
}
