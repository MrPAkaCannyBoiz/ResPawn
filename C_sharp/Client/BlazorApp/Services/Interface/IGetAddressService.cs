using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IGetAddressService
{
    Task<List<PawnshopAddressDto>> GetPawnshopAddressesAsync();
}
