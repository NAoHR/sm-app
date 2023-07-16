package najmi.learn.userserviceclient;

import lombok.extern.slf4j.Slf4j;
import najmi.learn.userserviceclient.dto.BaseResponse;
import najmi.learn.userserviceclient.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class UserServiceClient {
    @Autowired
    private UserServiceEndpoint userServiceEndpoint;
    public BaseResponse<UserResponse> getUser(String token){
        Response<BaseResponse<UserResponse>> response = null;

        try{
            response = userServiceEndpoint.getUser(token).execute();
        }catch (Exception e){
            log.error("Failed to call get user detail", e);
        }

        return Optional.of(response)
                .map(Response::body)
                .orElse(null);
    }

    public BaseResponse<UserResponse> findUser(UUID id){
        Response<BaseResponse<UserResponse>> response = null;

        try{
            response = userServiceEndpoint.findUser(id).execute();
        }catch (Exception e){
            log.error("Failed to call get user detail", e);
        }

        return Optional.of(response)
                .map(Response::body)
                .orElse(null);
    }
}
