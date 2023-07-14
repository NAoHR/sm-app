package najmi.learn.smservice.service;

import lombok.RequiredArgsConstructor;
import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.mapper.UserEntityMapper;
import najmi.learn.smservice.model.BaseResponse;
import najmi.learn.smservice.repo.UserEntityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserEntityRepository userEntityRepository;

    public BaseResponse<Object> findUser(UUID uuid) {
        UserEntity user = userEntityRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Not Found"));
        return BaseResponse.builder()
                .ok(true)
                .message("User Found")
                .error(null)
                .data(new UserEntityMapper().mapToUserEntityResponse(user))
                .build();
    }
}
