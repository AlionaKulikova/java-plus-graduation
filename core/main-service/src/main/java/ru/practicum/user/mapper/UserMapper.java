package ru.practicum.user.mapper;


import ru.practicum.user.dto.AdminUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserDtoReceived;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.stream.Collectors;


public class UserMapper {

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .id(user.getId())
                .build();
    }

    public static User toUser(UserDtoReceived userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .isAdmin(userDto.getIsAdmin())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static AdminUserDto toAdminUserDto(User newUser) {
        return AdminUserDto.builder()
                .email(newUser.getEmail())
                .id(newUser.getId())
                .name(newUser.getName())
                .build();
    }

    public static List<AdminUserDto> toListAdminUserDto(List<User> users) {
        return users.stream().map(UserMapper::toAdminUserDto).collect(Collectors.toList());
    }

}