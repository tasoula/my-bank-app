package io.github.tasoula.front_ui.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AdultValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Adult {
    String message() default "Вам должно быть не менее 18 лет";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
