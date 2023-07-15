package najmi.learn.userserviceclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponse <T> {
    private boolean ok;
    private String message;
    private T data;
    private String error;
}
