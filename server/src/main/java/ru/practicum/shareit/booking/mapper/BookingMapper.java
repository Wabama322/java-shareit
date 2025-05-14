package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

@UtilityClass
public class BookingMapper {
    public Booking toBooking(BookingDtoRequest bookingDtoRequest, Item item, User user) {
        Objects.requireNonNull(bookingDtoRequest, "BookingDtoRequest cannot be null");
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        return Booking.builder()
                .start(bookingDtoRequest.getStart())
                .end(bookingDtoRequest.getEnd())
                .item(item)
                .booker(user)
                .build();
    }

    public BookingForItemDto toItemBookingInfoDto(Booking booking) {
        if (booking == null) return null;

        return new BookingForItemDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public BookingForResponse toBookingForResponse(Booking booking) {
        if (booking == null) return null;

        return BookingForResponse.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(mapToItemWithBookingDto(booking.getItem()))
                .booker(mapToUserWithIdAndNameDto(booking.getBooker()))
                .build();
    }

    public BookingLastAndNextDto toItemBookingLastAndNextDto(Booking booking) {
        if (booking == null) return null;

        return BookingLastAndNextDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    private ItemWithBookingDto mapToItemWithBookingDto(Item item) {
        if (item == null) return null;

        return new ItemWithBookingDto(
                item.getId(),
                item.getName()
        );
    }

    private UserWithIdAndNameDto mapToUserWithIdAndNameDto(User user) {
        if (user == null) return null;

        return new UserWithIdAndNameDto(
                user.getId(),
                user.getName()
        );
    }
}