package org.example.respawnmarket.dtos;

import java.time.LocalDate;

public class ProductDto // class for transferring product data from UploadProductService response
{
    // product message (from proto file)
    private int id;
    private double price;
    private boolean sold;
    private String condition;
    private String approvalStatus;
    private String name;
    private String photoUrl;
    private String category;
    private String description;
    private int soldByCustomerId;
    private LocalDate registerDate;

    public ProductDto(int id, double price, boolean sold, String condition,
                      String approvalStatus, String name, String photoUrl,
                      String category, int soldByCustomerId,
                      String description, LocalDate registerDate)
    {
        this.id = id;
        this.price = price;
        this.sold = sold;
        this.condition = condition;
        this.approvalStatus = approvalStatus;
        this.name = name;
        this.photoUrl = photoUrl;
        this.category = category;
        this.soldByCustomerId = soldByCustomerId;
        this.description = description;
        this.registerDate = registerDate;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public boolean isSold()
    {
        return sold;
    }

    public void setSold(boolean sold)
    {
        this.sold = sold;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public String getApprovalStatus()
    {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus)
    {
        this.approvalStatus = approvalStatus;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public int getSoldByCustomerId()
    {
        return soldByCustomerId;
    }

    public void setSoldByCustomerId(int soldByCustomerId)
    {
        this.soldByCustomerId = soldByCustomerId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public LocalDate getRegisterDate()
    {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate)
    {
        this.registerDate = registerDate;
    }
}
