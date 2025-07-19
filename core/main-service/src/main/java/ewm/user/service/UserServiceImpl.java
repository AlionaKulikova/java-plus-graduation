package ewm.user.service;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import ewm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.error.exception.ValidationException;
import ewm.user.mapper.UserMapper;
import ewm.user.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры пагинации должны быть неотрицательными и size > 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users = (ids == null || ids.isEmpty()) ?
                userRepository.findAll(pageable).getContent() :
                userRepository.findByIdIn(ids, pageable).getContent();
        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto create(NewUserRequest newUser) {
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new ConflictException("Пользователь с email=" + newUser.getEmail() + " уже существует");
        }
        User user = userMapper.toUser(newUser);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        User user = checkUserExists(id);
        userRepository.deleteById(id);
        log.info("Пользователь {} удалён", user);
    }

    private User checkUserExists(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден или недоступен"));
    }
}