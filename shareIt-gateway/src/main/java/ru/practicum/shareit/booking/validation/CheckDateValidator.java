package ru.practicum.shareit.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;

import java.time.LocalDateTime;

public class CheckDateValidator implements ConstraintValidator<StartBeforeEndDateValid, BookingDtoRequest> {
    @Override
    public void initialize(StartBeforeEndDateValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(BookingDtoRequest bookingDtoRequest, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = bookingDtoRequest.getStart();
        LocalDateTime end = bookingDtoRequest.getEnd();
        Long itemId = bookingDtoRequest.getItemId();
        if (start == null || end == null || itemId == null) {
            return false;
        }
        if (!end.isAfter(start)) {
            return false;
        }
        return true;
    }
}
