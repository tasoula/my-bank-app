package io.github.tasoula.front_ui.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        if (birthdate == null) {
            return false; // Другие аннотации (@NotNull) обрабатывают пустые значения
        }

        LocalDate today = LocalDate.now();
        Period period = Period.between(birthdate, today);

        return period.getYears() >= 18;
    }
}
