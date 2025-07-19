package ewm.user.service;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> findAllBy(List<Long> ids, Pageable pageRequest);

    void deleteBy(long userId);

    UserDto findBy(long userId);
}
