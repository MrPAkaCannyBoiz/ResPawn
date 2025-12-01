package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "image")
public class ImageEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    public ImageEntity()
    {
    }

    public ImageEntity(String imageUrl, ProductEntity product)
    {
        this.imageUrl = imageUrl;
        this.product = product;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public ProductEntity getProduct()
    {
        return product;
    }

    public void setProduct(ProductEntity product)
    {
        this.product = product;
    }
}
