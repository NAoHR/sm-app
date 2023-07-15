package najmi.learn.userserviceclient;

import najmi.learn.userserviceclient.dto.BaseResponse;
import najmi.learn.userserviceclient.dto.UserResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserServiceEndpoint {
    @GET("api/v1/user")
    Call<BaseResponse<UserResponse>> getUser(
            @Header("Authorization") String token
    );
}
