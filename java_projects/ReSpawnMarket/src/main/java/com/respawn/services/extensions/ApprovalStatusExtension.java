package com.respawn.services.extensions;

import com.respawnmarket.ApprovalStatus;
import com.respawn.entities.enums.ApprovalStatusEnum;

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
