package ru.practicum.user.service;

import ru.practicum.user.dto.AdminUserDto;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {
    AdminUserDto createUser(User user);

    void deleteUser(Long userId);

    List<User> readUsers(List<Long> idList, int from, int size);

    User findUserById(Long id);
}