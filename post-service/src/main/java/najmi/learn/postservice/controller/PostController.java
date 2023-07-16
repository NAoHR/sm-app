package najmi.learn.postservice.controller;

import lombok.RequiredArgsConstructor;
import najmi.learn.postservice.model.PostRequest;
import najmi.learn.postservice.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/make")
    public ResponseEntity<Object> posting(@RequestBody PostRequest request, @RequestHeader("Authorization") String authorization){
        return ResponseEntity.ok(postService.postAPost(request, authorization));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPost(@PathVariable Integer id){
        return ResponseEntity.ok(postService.getPost(id));
    }
}
