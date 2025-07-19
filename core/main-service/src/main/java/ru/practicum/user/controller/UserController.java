package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.AdminUserDto;
import ru.practicum.user.dto.UserDtoReceived;
import ru.practicum.dto.Validator;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.user.mapper.UserMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/admin/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AdminUserDto> createUser(@RequestBody @Validated({Validator.Create.class}) UserDtoReceived userDto) {
        log.info("Calling the POST request to /admin/users endpoint {}", userDto);
        User user = UserMapper.toUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(user));

    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        log.info("Calling the DELETE request to /admin/users/{userId} endpoint");
        userService.deleteUser(userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("User deleted: " + userId);
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getUsers(@RequestParam(defaultValue = "") List<Long> ids,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Calling the GET request to /admin/users endpoint");
        List<AdminUserDto> response = UserMapper.toListAdminUserDto(userService.readUsers(ids, from, size));
        return ResponseEntity.ok(response);
    }

}