using System;

namespace ApiContracts.Dtos;

public class LatestProductFromInspectionDto
{
    public ProductWithFirstImageDto? ProductWithFirstImage {get; set;}
    public string InspectionComment {get; set;} = "";
}
