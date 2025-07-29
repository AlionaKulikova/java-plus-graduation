package ewm.user.service;

import ewm.interaction.dto.user.NewUserRequest;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> findAllBy(List<Long> ids, Pageable pageRequest);

    void deleteBy(long userId);

    UserDto findBy(long userId);

    Map<Long, UserShortDto> findAllBy(List<Long> ids);
}
