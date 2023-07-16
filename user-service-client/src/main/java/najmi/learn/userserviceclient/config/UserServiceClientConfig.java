package najmi.learn.userserviceclient.config;

import lombok.Data;
import najmi.learn.userserviceclient.UserServiceEndpoint;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "user.http-client")
public class UserServiceClientConfig {
    private String baseUrl;
    private long connectTimeoutInMilliseconds;
    private long readTimeoutInMilliseconds;
    private String logLevel;
    @Bean
    public OkHttpClient userHttpClient(UserServiceClientConfig configuration) {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(Level.valueOf(configuration.getLogLevel()));

        return new OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .connectTimeout(configuration.getConnectTimeoutInMilliseconds(), TimeUnit.MILLISECONDS)
                .readTimeout(configuration.getReadTimeoutInMilliseconds(), TimeUnit.MILLISECONDS)
                .writeTimeout(configuration.getReadTimeoutInMilliseconds(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public Retrofit userRetrofit(
         UserServiceClientConfig config,
         @Qualifier("userHttpClient") OkHttpClient httpClient
    ){
        Retrofit.Builder retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(config.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create());

        return retrofit.build();
    }

    @Bean
    public UserServiceEndpoint userEndpoint(@Qualifier("userRetrofit") Retrofit retrofit){
        return retrofit.create(UserServiceEndpoint.class);
    }

}
