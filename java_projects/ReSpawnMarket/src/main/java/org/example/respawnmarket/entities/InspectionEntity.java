package org.example.respawnmarket.entities;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;

@Entity
@Table(name = "inspection")
public class InspectionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "product_id", nullable = false)
    private ProductEntity product; // FK

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "inspected_by_reseller_id", nullable = false)
    private ResellerEntity reseller;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "comments", nullable = true)
    private String comment;

    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_stage", nullable = false)
    private ApprovalStatusEnum approvalStage;

    public InspectionEntity() {}

    public InspectionEntity(ProductEntity product, ResellerEntity reseller, String comment, boolean isAccepted)
    {
        this.product = product;
        this.reseller = reseller;
        this.inspectionDate = LocalDateTime.now();
        this.comment = comment;
        this.isAccepted = isAccepted;
        this.approvalStage = ApprovalStatusEnum.REVIEWING;
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

    public ApprovalStatusEnum getApprovalStage()
    {
        return approvalStage;
    }

    public void setApprovalStage(ApprovalStatusEnum approvalStage)
    {
        this.approvalStage = approvalStage;
    }

}