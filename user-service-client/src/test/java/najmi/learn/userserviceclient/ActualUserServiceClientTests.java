package najmi.learn.userserviceclient;

import najmi.learn.userserviceclient.dto.BaseResponse;
import najmi.learn.userserviceclient.dto.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;

public class ActualUserServiceClientTests {
    // mockito kinda silly bru, they aint actually end-to-end tes cuh. but good enough to produce an isolated test ><

    @InjectMocks
    private UserServiceClient userServiceClient;

    @Mock
    private UserServiceEndpoint userServiceEndpoint;

    @Mock
    private Call<BaseResponse<UserResponse>> getUserCall;


    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getUserWithTokenPresent() throws IOException {
        Response<BaseResponse<UserResponse>> getUserResponse = Response.success(BaseResponse.<UserResponse>builder()
                        .ok(true)
                        .message("message")
                        .data(UserResponse.builder()
                                .id(UUID.randomUUID())
                                .email("email@gmail.com")
                                .firstName("waduh")
                                .lastName("waduh")
                                .build())
                .build());

        Mockito.when(userServiceEndpoint.getUser(anyString())).thenReturn(getUserCall);
        Mockito.when(getUserCall.execute()).thenReturn(getUserResponse);

        final BaseResponse<UserResponse> actual = userServiceClient.getUser("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuYWptaUBnbWFpbC5jb20iLCJpYXQiOjE2ODk1MTQxNjUsImV4cCI6MTY4OTYwMDU2NX0.tCJ8mxQqTTwd85_XJeCVUXHxXvBiwxiEGOgEyCLE614");
        Assertions.assertNotNull(actual);
    }

    @Test
    public void getUserWithTokenUnPresent() throws IOException {
        Response<BaseResponse<UserResponse>> getUserResponse = Response.success(null);

        Mockito.when(userServiceEndpoint.getUser(anyString())).thenReturn(getUserCall);
        Mockito.when(getUserCall.execute()).thenReturn(getUserResponse);

        final BaseResponse<UserResponse> actual = userServiceClient.getUser("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuYWptaUBnbWFpbC5jb20iLCJpYXQiOjE2ODk1MTQxNjUsImV4cCI6MTY4OTYwMDU2NX0.tCJ8mxQqTTwd85_XJeCVUXHxXvBiwxiEGOgEyCLE614");
        Assertions.assertNull(actual);
    }
}
