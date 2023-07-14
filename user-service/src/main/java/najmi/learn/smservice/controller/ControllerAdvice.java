package najmi.learn.smservice.controller;

import najmi.learn.smservice.model.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> responseStatusHandler(ResponseStatusException e){
        return ResponseEntity.status(e.getStatus()).body(
                BaseResponse.builder()
                        .ok(false)
                        .message("Error Occured")
                        .error(e.getReason())
                        .data(null)
                        .build()
        );
    }
}
