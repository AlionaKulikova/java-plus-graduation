package ewm.interaction.feign;

import ewm.interaction.dto.user.NewUserRequest;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserFeignClient {
    @PostMapping
    UserDto create(@RequestBody NewUserRequest newUserRequest);

    @GetMapping
    List<UserDto> findAllBy(@RequestParam(required = false) List<Long> ids,
                            @RequestParam(defaultValue = "0") int from,
                            @RequestParam(defaultValue = "10") int size);

    @DeleteMapping("/{userId}")
    void deleteBy(@PathVariable Long userId);

    @GetMapping("/mapped")
    Map<Long, UserShortDto> userMapBy(@RequestParam List<Long> ids);
}
