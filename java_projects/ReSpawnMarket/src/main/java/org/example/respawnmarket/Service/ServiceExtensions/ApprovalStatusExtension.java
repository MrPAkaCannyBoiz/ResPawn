package org.example.respawnmarket.Service.ServiceExtensions;

import com.respawnmarket.ApprovalStatus;
import com.respawnmarket.Category;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;

public class ApprovalStatusExtension
{
    public static ApprovalStatus toProtoApprovalStatus(ApprovalStatusEnum entityApprovalStatus)
    {
        return switch (entityApprovalStatus)
        {
            case PENDING -> com.respawnmarket.ApprovalStatus.PENDING;
            case APPROVED -> com.respawnmarket.ApprovalStatus.APPROVED;
            case REVIEWING -> com.respawnmarket.ApprovalStatus.REVIEWING;
            case REJECTED -> com.respawnmarket.ApprovalStatus.REJECTED;
        };
    }

}
