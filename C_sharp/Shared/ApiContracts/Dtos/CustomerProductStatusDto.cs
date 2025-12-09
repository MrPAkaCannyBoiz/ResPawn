using System;
using ApiContracts.Dtos.Enums;

namespace ApiContracts.Dtos;

public class CustomerProductStatusDto
{
    public int ProductId {get;set;}
    public string? Name {get;set;}
    public ApprovalStatus ApprovalStatus {get;set;}
    public string? LatestComments {get;set;}
}
