package najmi.learn.postservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import najmi.learn.userserviceclient.dto.UserResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Integer id;
    private String title;
    private String description;
    private Long view;
    @JsonProperty("belongs_to")
    private UserResponse userResponse;
}
