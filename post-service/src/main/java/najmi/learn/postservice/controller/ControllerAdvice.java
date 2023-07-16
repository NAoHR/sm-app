package najmi.learn.postservice.controller;

import lombok.extern.slf4j.Slf4j;
import najmi.learn.postservice.model.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> otherErrorHandler(Exception e){
        log.error("Error: {}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                BaseResponse.builder()
                        .ok(false)
                        .message("Internal Error")
                        .error(e.getMessage())
                        .data(null)
                        .build()
        );
    }
}

