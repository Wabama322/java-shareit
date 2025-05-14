package ru.practicum.shareit.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;

public class CheckDateValidator implements ConstraintValidator<StartBeforeEndDateValid, BookingDtoRequest> {

    @Override
    public boolean isValid(BookingDtoRequest booking, ConstraintValidatorContext context) {
        if (booking == null) {
            return true;
        }

        if (booking.getItemId() == null) {
            addConstraintViolation(context, "ID вещи должен быть указан");
            return false;
        }

        if (booking.getStart() == null || booking.getEnd() == null) {
            addConstraintViolation(context, "Даты начала и окончания должны быть указаны");
            return false;
        }

        if (!booking.getEnd().isAfter(booking.getStart())) {
            addConstraintViolation(context, "Дата окончания должна быть после даты начала");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}