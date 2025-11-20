package org.example.respawnmarket.entities;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection")
public class InspectionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @MapsId("id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "product_id", nullable = false)
    private ProductEntity product; // FK

    @MapsId("id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "inspected_by_reseller_id", nullable = false)
    private ResellerEntity reseller;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "result")
    private String result;

    @Column(name = "comments", nullable = true)
    private String comment;

    private boolean isAccepted;

    public InspectionEntity() {}

    public InspectionEntity(ProductEntity product, ResellerEntity reseller,
                            String result, String comment,  boolean isAccepted) {
        this.product = product;
        this.reseller = reseller;
        this.inspectionDate = LocalDateTime.now();
        this.result = result;
        this.comment = comment;
        this.isAccepted = isAccepted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public ResellerEntity getReseller() {
        return reseller;
    }

    public void setReseller(ResellerEntity reseller) {
        this.reseller = reseller;
    }

    public LocalDateTime getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDateTime inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}