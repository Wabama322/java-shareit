package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";
    private static final String STATE_PARAM = "state";
    private static final String FROM_PARAM = "from";
    private static final String SIZE_PARAM = "size";
    private static final String APPROVED_PARAM = "approved";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getAllBookingsByUser(long userId, BookingState state, int from, int size) {
        return getWithPagination("", userId, state, from, size);
    }

    public ResponseEntity<Object> addBooking(long userId, BookingDtoRequest requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> approveBooking(long userId, long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                APPROVED_PARAM, approved
        );
        return patch("/" + bookingId + "?" + APPROVED_PARAM + "={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getAllBookingsByOwner(long userId, BookingState state, int from, int size) {
        return getWithPagination("/owner", userId, state, from, size);
    }

    private ResponseEntity<Object> getWithPagination(String path, long userId, BookingState state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                STATE_PARAM, state.name(),
                FROM_PARAM, from,
                SIZE_PARAM, size
        );
        String queryString = String.format("?%s={%s}&%s={%s}&%s={%s}",
                STATE_PARAM, STATE_PARAM,
                FROM_PARAM, FROM_PARAM,
                SIZE_PARAM, SIZE_PARAM);
        return get(path + queryString, userId, parameters);
    }
}