package com.group_1.album.exception;

import com.group_1.sharedDynamoDB.exception.NoSuchElementFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class MasterExceptionController {
    @ExceptionHandler(NoSuchElementFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "NOT_FOUND")
    public void handleNoElementException(
            NoSuchElementFoundException exception
    ) {
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "ALBUM_PASSWORD_NOT_MATCH")
    public void handlePasswordAlbumException(AlbumPasswordNotMatchException exception
    ) {
    }
}

