package org.example.respawnmarket.entities;

import jakarta.persistence.*;
import org.example.respawnmarket.entities.enums.ApprovalStatus;
import org.example.respawnmarket.entities.enums.Category;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")

public class ProductEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "name", nullable = false)
    private String name;

    @Column (name = "price", nullable = false)
    private double price;

    @Column (name = "sold", nullable = false)
    private boolean sold;

    @Column (name = "condition", nullable = false)
    private String condition;

    @Enumerated(EnumType.STRING)
    @Column (name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column (name = "category", nullable = false)
    private Category category;

    @Column (name = "description", nullable = false)
    private String description;

    @Column (name = "photo_url", nullable = true)
    private String photoUrl;

    @Column (name = "register_date", nullable = false)
    private LocalDateTime registerDate;

    @Column (name = "other_category", nullable = true)
    private String otherCategory;

    //FK
    @ManyToOne
    @JoinColumn(name = "sold_by_customer", nullable = false)
    private CustomerEntity seller;

   public ProductEntity()
   {
   }

    public ProductEntity(String name, double price, String condition, String description,
                         String photoUrl, CustomerEntity seller, Category category)
    {
        this.name = name;
        this.price = price;
        this.condition = condition;
        this.description = description;
        this.photoUrl = photoUrl;
        this.seller = seller;
        this.sold = false;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.registerDate = LocalDateTime.now();
        this.category = category;
        this.otherCategory = "";
    }

    public ProductEntity(String name, double price, String condition, String description,
                         String photoUrl, CustomerEntity seller, String otherCategory)
    {
        this.name = name;
        this.price = price;
        this.condition = condition;
        this.description = description;
        this.photoUrl = photoUrl;
        this.seller = seller;
        this.sold = false;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.registerDate = LocalDateTime.now();
        this.category = Category.OTHER;
        this.otherCategory = otherCategory;
    }


    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public double getPrice()
    {
        return price;
    }

    public String getCondition()
    {
        return condition;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }


    public CustomerEntity getSeller()
    {
        return seller;
    }

    public void setSeller(CustomerEntity seller)
    {
        this.seller = seller;
    }

    public LocalDateTime getRegisterDate()
    {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate)
    {
        this.registerDate = registerDate;
    }

    public Category getCategory()
    {
        return category;
    }

    public void setCategory(Category category)
    {
        this.category = category;
    }

    public ApprovalStatus getApprovalStatus()
    {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus)
    {
        this.approvalStatus = approvalStatus;
    }

    public boolean isSold()
    {
        return sold;
    }

    public void setSold(boolean sold)
    {
        this.sold = sold;
    }

}
