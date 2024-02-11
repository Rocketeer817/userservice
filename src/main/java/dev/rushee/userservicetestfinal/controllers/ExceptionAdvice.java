package dev.rushee.userservicetestfinal.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler({IllegalArgumentException.class,Exception.class})
    public ResponseEntity<String> handleException(Exception e){
        return new ResponseEntity<>("We will be back in a short time", HttpStatus.NOT_FOUND);
    }
}
