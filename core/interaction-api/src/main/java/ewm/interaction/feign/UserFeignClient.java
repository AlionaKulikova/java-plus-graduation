package ewm.interaction.feign;

import ewm.interaction.dto.user.NewUserRequest;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.util.PathConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", path = PathConstants.ADMIN_USERS)
public interface UserFeignClient {
    @PostMapping
    UserDto createUser(@RequestBody NewUserRequest newUserRequest);

    @GetMapping
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                            @RequestParam(defaultValue = "0") int from,
                            @RequestParam(defaultValue = "10") int size);

    @DeleteMapping(PathConstants.USER_ID)
    void deleteUser(@PathVariable Long userId);

    @GetMapping(PathConstants.USER_MAPPED)
    Map<Long, UserShortDto> getUsersByIDS(@RequestParam List<Long> ids);
}