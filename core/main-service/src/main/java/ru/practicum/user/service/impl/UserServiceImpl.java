package ru.practicum.user.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.NotUniqueException;
import ru.practicum.user.dto.AdminUserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserMainServiceRepository;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserMainServiceRepository userMainServiceRepository;

    @Transactional
    @Override
    public AdminUserDto createUser(User user) {
        log.info("createUser - invoked");
        if (userMainServiceRepository.existsByEmail(user.getEmail())) {
            throw new NotUniqueException("User with this email already exists");
        }
        AdminUserDto adminDto = UserMapper.toAdminUserDto(userMainServiceRepository.save(user));
        log.info("Result: user {} - created", adminDto);
        return adminDto;
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        log.info("deleteUser - invoked");
        if (!userMainServiceRepository.existsById(userId)) {
            log.error("User with id = {} not exist", userId);
            throw new NotFoundException("User not found");
        }
        log.info("Result: user with id = {} - removed", userId);
        userMainServiceRepository.deleteById(userId);
    }

    @Override
    public List<User> readUsers(List<Long> idList, int from, int size) {
        log.info("readUsers - invoked");
        Pageable pageable = createPageRequestAsc(from, size);

        if (idList.isEmpty()) {
            List<User> allUsers = userMainServiceRepository.findAllUser(pageable);
            log.info("Result: idList is empty, returning all users size = {}", allUsers.size());
            return allUsers;
        }

        List<User> users = userMainServiceRepository.findAllByIdIn(idList, pageable);
        if (users.isEmpty()) {
            return List.of();
        }
        log.info("Result: list of Users size = {}", users.size());
        return users;
    }

    private static PageRequest createPageRequestAsc(int from, int size) {
        return PageRequest.of(from, size, Sort.Direction.ASC, "id");
    }

    @Override
    public User findUserById(Long id) {
        Optional<User> user = userMainServiceRepository.findById(id);

        if (user.isEmpty()) {
            log.error("User with id {} not found", id);
            throw new NotFoundException(String.format("User with id=%s was not found", id));
        }

        log.info("User with id {} found", id);
        return user.get();
    }
}