package ru.practicum.shareit.booking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = CheckDateValidator.class)
@Repeatable(StartBeforeEndDateValid.List.class) // Добавляем поддержку повторяемости
public @interface StartBeforeEndDateValid {
    String message() default "Invalid booking dates: {detailedMessage}";

    boolean checkPastDates() default true;
    long minDurationHours() default 1;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        StartBeforeEndDateValid[] value();
    }
}