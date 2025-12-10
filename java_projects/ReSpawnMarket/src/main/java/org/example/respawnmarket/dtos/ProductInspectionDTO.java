package org.example.respawnmarket.dtos;

import org.example.respawnmarket.entities.ProductEntity;

public class ProductInspectionDTO
{
  private ProductEntity product;
  private String latestComment;

  public ProductInspectionDTO(ProductEntity product, String latestComment)
  {
    this.product = product;
    this.latestComment = latestComment;
  }

  public ProductEntity getProduct() {
    return product;
  }

  public String getLatestComment()
  {
    return latestComment;
  }

  public void setLatestComment(String latestComment)
  {
    this.latestComment = latestComment;
  }

  public void setProduct(ProductEntity product)
  {
    this.product = product;
  }
}
