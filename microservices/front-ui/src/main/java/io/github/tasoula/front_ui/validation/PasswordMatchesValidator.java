package io.github.tasoula.front_ui.validation;

import io.github.tasoula.front_ui.dto.PasswordChangeDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        PasswordChangeDto user = (PasswordChangeDto) obj;
        return user.getPassword().equals(user.getConfirm_password());
    }
}
