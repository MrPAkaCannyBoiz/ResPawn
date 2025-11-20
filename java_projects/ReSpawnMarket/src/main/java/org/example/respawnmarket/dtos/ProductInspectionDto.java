package org.example.respawnmarket.dtos;

public class ProductInspectionDto {
    private int productId;
    private int resellerId;
    private String status;
    private String comment;

    public ProductInspectionDto(int productId, int resellerId, String status, String comment)
    {
        this.productId = productId;
        this.resellerId = resellerId;
        this.status = status;
        this.comment = comment;
    }

    public int getProductId()
    {
        return productId;
    }

    public void setProductId(int productId)
    {
        this.productId = productId;
    }

    public int getResellerId()
    {
        return resellerId;
    }

    public void setResellerId(int resellerId)
    {
        this.resellerId = resellerId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }
}