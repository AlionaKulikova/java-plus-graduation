package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.util.PathConstants.ADMIN_USERS;
import static ru.practicum.util.PathConstants.USER_ID;

@Slf4j
@RestController
@RequestMapping(ADMIN_USERS)
@RequiredArgsConstructor
@Validated
public class UserControllerAdmin {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Эндпоинт /admin/users. GET запрос на получение админом списка из {} пользователей, начиная с {}",
                size, from);
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest newUser) {
        log.info("Эндпоинт /admin/users. POST запрос на создание админом пользователя {}.", newUser);
        return userService.create(newUser);
    }

    @DeleteMapping(USER_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long userId) {
        log.info("Эндпоинт /admin/users/{userId}. DELETE запрос на удаление админом пользователя с id {}.", userId);
        userService.delete(userId);
    }
}