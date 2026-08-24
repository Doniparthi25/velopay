package com.codingshuttle.razorpay.vault_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(
        validatedBy = {ExpiryYearValidator.class}
)
@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpiryYear {
    String message() default "Expiry year cannot be in past";
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
