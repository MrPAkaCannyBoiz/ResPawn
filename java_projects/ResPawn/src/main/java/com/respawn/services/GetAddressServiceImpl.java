package com.respawn.services;

import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import com.respawn.dtos.PawnshopAddressPostalDto;
import com.respawn.repositories.AddressRepository;
import com.respawn.repositories.PawnshopRepository;
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
                .setId(dto.addressId())
                .setStreetName(dto.streetName())
                .setSecondaryUnit(dto.secondaryUnit())
                .setPostalCode(dto.postalCode())
                .build();
        Postal postalProto = Postal.newBuilder()
                .setPostalCode(dto.postalCode())
                .setCity(dto.city())
                .build();

        return PawnshopAddressWithPostal.newBuilder()
                .setAddress(addressProto)
                .setPostal(postalProto)
                .setPawnshopId(dto.pawnshopId())
                .build();
    }
}
