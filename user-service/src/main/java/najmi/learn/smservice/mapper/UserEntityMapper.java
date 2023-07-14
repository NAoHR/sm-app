package najmi.learn.smservice.mapper;


import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.model.UserResponse;

public class UserEntityMapper{

    public UserResponse mapToUserEntityResponse(UserEntity user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .build();
    }
}
