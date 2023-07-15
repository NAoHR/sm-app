package najmi.learn.userserviceclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import najmi.learn.userserviceclient.UserServiceEndpoint;
import najmi.learn.userserviceclient.dto.BaseResponse;
import najmi.learn.userserviceclient.dto.UserResponse;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserRestClient {
    private final UserServiceEndpoint userServiceEndpoint;
    public BaseResponse<UserResponse> getUser(String token){
        Response<BaseResponse<UserResponse>> response = null;

        try{
            response = userServiceEndpoint.getUser("Bearer "+token).execute();
        }catch (Exception e){
            log.error("Failed to call get user detail", e);
        }

        return Optional.of(response)
                .map(Response::body)
                .orElse(null);
    }
}
