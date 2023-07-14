package najmi.learn.smservice.controller;

import lombok.RequiredArgsConstructor;
import najmi.learn.smservice.model.AuthenticationResponse;
import najmi.learn.smservice.model.LoginRequest;
import najmi.learn.smservice.model.RegisterRequest;
import najmi.learn.smservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestBody LoginRequest request
    ){
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/waduh")
    public ResponseEntity<Object> waduh(){
        return ResponseEntity.ok(null);
    }
}
