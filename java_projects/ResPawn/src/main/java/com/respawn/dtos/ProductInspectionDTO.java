package com.respawn.dtos;

import com.respawn.entities.ProductEntity;

public record ProductInspectionDTO(ProductEntity product, String latestComment)
{
  public ProductInspectionDTO(ProductEntity product, String latestComment)
  {
    String latestComment1;
    this.product = product;
    latestComment1 = latestComment;
    if (latestComment == null)
    {
      latestComment1 = "";
    }
    this.latestComment = latestComment1;
  }

}
