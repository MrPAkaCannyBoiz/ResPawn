package org.example.respawnmarket.entities;

import java.time.LocalDateTime;

import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    private ApprovalStatusEnum approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column (name = "category", nullable = false)
    private CategoryEnum category;

    @Column (name = "description", nullable = false)
    private String description;

    @Column (name = "photo_url", nullable = true)
    private String photoUrl;

    @Column (name = "register_date", nullable = false)
    private LocalDateTime registerDate;

    @ManyToOne
    @JoinColumn(name = "sold_by_customer", nullable = false)
    private CustomerEntity seller;

    @Column(name = "reseller_comment", nullable = true)
    private String resellerComment;

    @Column(name = "inspected_by_reseller_id", nullable = true)
    private Integer inspectedByResellerId;

    public ProductEntity()
    {
    }

    public ProductEntity(String name, double price, String condition, String description,
                         String photoUrl, CustomerEntity seller, CategoryEnum category)
    {
        this.name = name;
        this.price = price;
        this.condition = condition;
        this.description = description;
        this.photoUrl = photoUrl;
        this.seller = seller;
        this.sold = false;
        this.approvalStatus = ApprovalStatusEnum.PENDING;
        this.registerDate = LocalDateTime.now();
        this.category = category;
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
        this.approvalStatus = ApprovalStatusEnum.PENDING;
        this.registerDate = LocalDateTime.now();
        this.category = CategoryEnum.OTHER;
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

    public CategoryEnum getCategory()
    {
        return category;
    }

    public void setCategory(CategoryEnum category)
    {
        this.category = category;
    }

    public ApprovalStatusEnum getApprovalStatus()
    {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatusEnum approvalStatus)
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

    public String getResellerComment()
    {
        return resellerComment;
    }

    public void setResellerComment(String resellerComment)
    {
        this.resellerComment = resellerComment;
    }

    public Integer getInspectedByResellerId()
    {
        return inspectedByResellerId;
    }

    public void setInspectedByResellerId(Integer inspectedByResellerId)
    {
        this.inspectedByResellerId = inspectedByResellerId;
    }
}
