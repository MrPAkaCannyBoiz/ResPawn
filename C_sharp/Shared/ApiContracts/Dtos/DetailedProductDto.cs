using ApiContracts.Dtos.Enums;
using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class DetailedProductDto
{
    public required int ProductId { get; set; }
    public required double Price { get; set; }
    public required bool Sold { get; set; }
    public required string Condition { get; set; }
    public string ApprovalStatus { get; set; } = string.Empty;
    public required string Name { get; set; }
    public required string PhotoUrl { get; set; }
    public string Category { get; set; } = string.Empty;
    public required string Description { get; set; }
    public required int SoldByCustomerId { get; set; }
    public required DateTime RegisterDate { get; set; }
    public required int SellerId { get; set; }
    public required string SellerFirstName { get; set; }
    public required string SellerLastName { get; set; }
    public string SellerEmail { get; set; } = string.Empty;
    public string SellerPhoneNumber { get; set; } = string.Empty;
    public required int PawnshopId { get; set; }
    public required string PawnshopName { get; set; }
    public required int PawnshopAddressId { get; set; }
    public required string PawnshopStreetName { get; set; }
    public string PawnshopSecondaryUnit { get; set; } = string.Empty;
    public required int PawnshopPostalCode { get; set; }
    public required string PawnshopCity { get; set; }

}
