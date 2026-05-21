package com.example.myspringproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Some error message")
public class UserExistException extends RuntimeException {
    public UserExistException() {
        
    }
}
