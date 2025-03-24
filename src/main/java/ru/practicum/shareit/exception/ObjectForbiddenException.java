package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ObjectForbiddenException extends RuntimeException {
    public ObjectForbiddenException(String message) {
        super(message);
    }
}
