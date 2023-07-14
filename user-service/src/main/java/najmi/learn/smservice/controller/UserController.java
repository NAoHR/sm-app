package najmi.learn.smservice.controller;

import lombok.RequiredArgsConstructor;

import najmi.learn.smservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @GetMapping("/user/{id}")
    public ResponseEntity<Object> getUser(@PathVariable("id") UUID uuid){
        return ResponseEntity.ok(userService.findUser(uuid));
    }

    @GetMapping("/user")
    public ResponseEntity<Object> user(
            @RequestAttribute("X-api-user-id") UUID uuid
    ){
        return ResponseEntity.ok(userService.findUser(uuid));
    }
}
