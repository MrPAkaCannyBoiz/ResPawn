package org.example.respawnmarket.Service.ServiceExtensions;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public class CustomerExceptionExtension
{
    public static StatusRuntimeException mapDataIntegrityViolation(DataIntegrityViolationException ex)
    {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof ConstraintViolationException cve) {
                return mapConstraintViolation(cve);
            }
            t = t.getCause();
        }
        assert ex != null;
        String msg = ex.getMessage();
        if (msg != null && msg.contains("customer_email_key")) {
            return Status.ALREADY_EXISTS
                    .withDescription("Email already in use")
                    .withCause(ex)
                    .asRuntimeException();
        }
        if (msg != null && msg.contains("customer_phone_number_key")) {
            return Status.ALREADY_EXISTS
                    .withDescription("Phone number already in use")
                    .withCause(ex)
                    .asRuntimeException();
        }
        if (msg != null && msg.contains("customer_email_check"))
        {
            return Status.INVALID_ARGUMENT
                    .withDescription("Email is not valid")
                    .withCause(ex)
                    .asRuntimeException();
        }

        return Status.INVALID_ARGUMENT
                .withDescription("Request violates database constraints")
                .withCause(ex)
                .asRuntimeException();
    }

    public static StatusRuntimeException mapConstraintViolation(ConstraintViolationException cve)
    {
        String constraint = cve.getConstraintName();
        if ("customer_phone_number_key".equals(constraint))
        {
            return Status.ALREADY_EXISTS
                    .withDescription("Phone number already in use")
                    .withCause(cve)
                    .asRuntimeException();
        }
        else if ("customer_email_key".equals(constraint))
        {
            return Status.ALREADY_EXISTS
                    .withDescription("Email already in use")
                    .withCause(cve)
                    .asRuntimeException();
        }
        else if ("customer_email_check".equals(constraint))
        {
            return Status.INVALID_ARGUMENT
                    .withDescription("Email is not valid")
                    .withCause(cve)
                    .asRuntimeException();
        }

        return Status.INVALID_ARGUMENT
                .withDescription("Request violates database constraints: " + constraint)
                .withCause(cve)
                .asRuntimeException();
    }
}
