package com.respawn.services.extensions;

import com.respawn.entities.ImageEntity;
import com.respawn.entities.ProductEntity;
import com.respawn.entities.enums.ApprovalStatusEnum;
import com.respawn.entities.enums.CategoryEnum;
import com.respawnmarket.ApprovalStatus;
import com.respawnmarket.Category;
import com.respawnmarket.Image;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static com.respawn.services.extensions.ApprovalStatusExtension.toProtoApprovalStatus;
import static com.respawn.services.extensions.CategoryExtension.toEntityCategory;
import static com.respawn.services.extensions.CategoryExtension.toProtoCategory;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionTests {

    // ── ApprovalStatusExtension ────────────────────────────────────────────

    @Test
    void toProtoApprovalStatus_pending() {
        assertEquals(ApprovalStatus.PENDING, toProtoApprovalStatus(ApprovalStatusEnum.PENDING));
    }

    @Test
    void toProtoApprovalStatus_approved() {
        assertEquals(ApprovalStatus.APPROVED, toProtoApprovalStatus(ApprovalStatusEnum.APPROVED));
    }

    @Test
    void toProtoApprovalStatus_reviewing() {
        assertEquals(ApprovalStatus.REVIEWING, toProtoApprovalStatus(ApprovalStatusEnum.REVIEWING));
    }

    @Test
    void toProtoApprovalStatus_rejected() {
        assertEquals(ApprovalStatus.REJECTED, toProtoApprovalStatus(ApprovalStatusEnum.REJECTED));
    }

    // ── CategoryExtension ─────────────────────────────────────────────────

    @Test
    void toProtoCategory_null_returnsUnspecified() {
        assertEquals(Category.CATEGORY_UNSPECIFIED, toProtoCategory(null));
    }

    @ParameterizedTest
    @EnumSource(CategoryEnum.class)
    void toProtoCategory_allEnumValues_doNotThrow(CategoryEnum entity) {
        // Each entity category must map to a proto category without error
        Category proto = toProtoCategory(entity);
        assertNotNull(proto);
        assertNotEquals(Category.CATEGORY_UNSPECIFIED, proto);
    }

    @Test
    void toEntityCategory_unspecified_returnsNull() {
        assertNull(toEntityCategory(Category.CATEGORY_UNSPECIFIED));
    }

    @Test
    void toEntityCategory_electronics_returnsElectronics() {
        assertEquals(CategoryEnum.ELECTRONICS, toEntityCategory(Category.ELECTRONICS));
    }

    @Test
    void toEntityCategory_jewelry_returnsJewelry() {
        assertEquals(CategoryEnum.JEWELRY, toEntityCategory(Category.JEWELRY));
    }

    @Test
    void toEntityCategory_unrecognized_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> toEntityCategory(Category.UNRECOGNIZED));
    }

    @Test
    void toEntityCategory_allMappedProtoValues_doNotThrow() {
        // Spot-check several to ensure the switch is exhaustive
        assertNotNull(toEntityCategory(Category.WATCHES));
        assertNotNull(toEntityCategory(Category.GOLD_AND_SILVER));
        assertNotNull(toEntityCategory(Category.GAMING_CONSOLES));
        assertNotNull(toEntityCategory(Category.LUXURY_ITEMS));
        assertNotNull(toEntityCategory(Category.OTHER));
    }

    // ── CustomerExceptionExtension ────────────────────────────────────────

    @Test
    void mapDataIntegrityViolation_emailKey_returnsAlreadyExists() {
        var ex = new DataIntegrityViolationException("customer_email_key constraint");
        StatusRuntimeException result = CustomerExceptionExtension.mapDataIntegrityViolation(ex);
        assertEquals(Status.ALREADY_EXISTS.getCode(), result.getStatus().getCode());
        assertEquals("Email already in use", result.getStatus().getDescription());
    }

    @Test
    void mapDataIntegrityViolation_phoneKey_returnsAlreadyExists() {
        var ex = new DataIntegrityViolationException("customer_phone_number_key constraint");
        StatusRuntimeException result = CustomerExceptionExtension.mapDataIntegrityViolation(ex);
        assertEquals(Status.ALREADY_EXISTS.getCode(), result.getStatus().getCode());
        assertEquals("Phone number already in use", result.getStatus().getDescription());
    }

    @Test
    void mapDataIntegrityViolation_emailCheck_returnsInvalidArgument() {
        var ex = new DataIntegrityViolationException("customer_email_check constraint");
        StatusRuntimeException result = CustomerExceptionExtension.mapDataIntegrityViolation(ex);
        assertEquals(Status.INVALID_ARGUMENT.getCode(), result.getStatus().getCode());
        assertEquals("Email is not valid", result.getStatus().getDescription());
    }

    @Test
    void mapDataIntegrityViolation_generic_returnsInvalidArgument() {
        var ex = new DataIntegrityViolationException("some_other_constraint");
        StatusRuntimeException result = CustomerExceptionExtension.mapDataIntegrityViolation(ex);
        assertEquals(Status.INVALID_ARGUMENT.getCode(), result.getStatus().getCode());
        assertEquals("Request violates database constraints", result.getStatus().getDescription());
    }

    @Test
    void mapDataIntegrityViolation_withConstraintViolationCause_delegatesToMapConstraintViolation() {
        var cve = new ConstraintViolationException("cve", null, "customer_email_key");
        var ex = new DataIntegrityViolationException("wrapped", cve);
        StatusRuntimeException result = CustomerExceptionExtension.mapDataIntegrityViolation(ex);
        // Should delegate to mapConstraintViolation which returns ALREADY_EXISTS for email key
        assertEquals(Status.ALREADY_EXISTS.getCode(), result.getStatus().getCode());
        assertEquals("Email already in use", result.getStatus().getDescription());
    }

    @Test
    void mapConstraintViolation_phoneKey_returnsAlreadyExists() {
        var cve = new ConstraintViolationException("cve", null, "customer_phone_number_key");
        StatusRuntimeException result = CustomerExceptionExtension.mapConstraintViolation(cve);
        assertEquals(Status.ALREADY_EXISTS.getCode(), result.getStatus().getCode());
        assertEquals("Phone number already in use", result.getStatus().getDescription());
    }

    @Test
    void mapConstraintViolation_emailKey_returnsAlreadyExists() {
        var cve = new ConstraintViolationException("cve", null, "customer_email_key");
        StatusRuntimeException result = CustomerExceptionExtension.mapConstraintViolation(cve);
        assertEquals(Status.ALREADY_EXISTS.getCode(), result.getStatus().getCode());
        assertEquals("Email already in use", result.getStatus().getDescription());
    }

    @Test
    void mapConstraintViolation_emailCheck_returnsInvalidArgument() {
        var cve = new ConstraintViolationException("cve", null, "customer_email_check");
        StatusRuntimeException result = CustomerExceptionExtension.mapConstraintViolation(cve);
        assertEquals(Status.INVALID_ARGUMENT.getCode(), result.getStatus().getCode());
        assertEquals("Email is not valid", result.getStatus().getDescription());
    }

    @Test
    void mapConstraintViolation_unknownConstraint_returnsInvalidArgument() {
        var cve = new ConstraintViolationException("cve", null, "some_unknown_key");
        StatusRuntimeException result = CustomerExceptionExtension.mapConstraintViolation(cve);
        assertEquals(Status.INVALID_ARGUMENT.getCode(), result.getStatus().getCode());
        assertTrue(result.getStatus().getDescription().contains("some_unknown_key"));
    }

    // ── ImageExtension ────────────────────────────────────────────────────

    @Test
    void toProtoImageList_nonEmpty_mapsCorrectly() {
        ProductEntity product = new ProductEntity();
        product.setId(5);

        ImageEntity entity = new ImageEntity("http://img.com/photo.jpg", product);
        entity.setId(42);

        List<Image> result = ImageExtension.toProtoImageList(List.of(entity));

        assertEquals(1, result.size());
        assertEquals(42, result.get(0).getId());
        assertEquals("http://img.com/photo.jpg", result.get(0).getUrl());
        assertEquals(5, result.get(0).getProductId());
    }

    @Test
    void toProtoImageList_empty_returnsEmptyList() {
        List<Image> result = ImageExtension.toProtoImageList(List.of());
        assertTrue(result.isEmpty());
    }
}
