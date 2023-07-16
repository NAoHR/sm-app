package najmi.learn.userserviceclient;

import najmi.learn.userserviceclient.dto.BaseResponse;
import najmi.learn.userserviceclient.dto.UserResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import java.util.UUID;

public interface UserServiceEndpoint {
    @GET("api/v1/user")
    Call<BaseResponse<UserResponse>> getUser(
            @Header("Authorization") String token
    );

    @GET("api/v1/user/{id}")
    Call<BaseResponse<UserResponse>> findUser(
            @Path("id") UUID id
            );
}
