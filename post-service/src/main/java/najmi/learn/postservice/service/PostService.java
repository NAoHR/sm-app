package najmi.learn.postservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import najmi.learn.postservice.entity.PostEntity;
import najmi.learn.postservice.model.PostRequest;
import najmi.learn.postservice.model.PostResponse;
import najmi.learn.postservice.repo.PostRepo;
import najmi.learn.userserviceclient.UserServiceClient;
import najmi.learn.userserviceclient.dto.BaseResponse;
import najmi.learn.userserviceclient.dto.UserResponse;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final RedisOperations<String, String> redisOperations;
    private final PostRepo postRepo;

    private final UserServiceClient userServiceClient;

    public PostEntity postAPost(PostRequest postRequest, String authorization){
        BaseResponse<UserResponse> user = userServiceClient.getUser(authorization);
        PostEntity post = PostEntity.builder()
                .owner(user.getData().getId())
                .title(postRequest.getTitle())
                .description(postRequest.getDescription())
                .build();
        post.setId(postRepo.save(post).getId());

        redisOperations.opsForValue().set(String.format("post:%s:view", post.getId()), "1");
        return post;
    }

    public PostResponse getPost(Integer id){
        PostEntity post = postRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
        BaseResponse<UserResponse> user = userServiceClient.findUser(post.getOwner());
        redisOperations.opsForValue().increment(String.format("post:%s:view", post.getId()));
        Long view = Long.valueOf(Optional.ofNullable(redisOperations.opsForValue().get(String.format("post:%s:view",post.getId()))).orElse("0"));

        return PostResponse.builder()
                .id(post.getId())
                .view(view)
                .description(post.getDescription())
                .userResponse(user.getData())
                .build();
    }

    public List<PostEntity> getAllUserPost(){
        return null;
    }
}
