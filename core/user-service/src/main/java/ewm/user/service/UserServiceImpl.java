package ewm.user.service;

import com.querydsl.core.BooleanBuilder;
import ewm.interaction.dto.user.NewUserRequest;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.NotFoundException;
import ewm.user.mappers.UserMapper;
import ewm.user.model.QUser;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;
    final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        User user = userMapper.toUser(newUserRequest);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> findAllBy(List<Long> ids, Pageable pageRequest) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (ids != null && !ids.isEmpty()) {
            booleanBuilder.and(QUser.user.id.in(ids));
        }

        Page<User> usersPage = userRepository.findAll(booleanBuilder, pageRequest);
        return usersPage.map(userMapper::toUserDto).toList();
    }

    @Override
    public Map<Long, UserShortDto> findAllBy(List<Long> ids) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (ids != null && !ids.isEmpty()) {
            booleanBuilder.and(QUser.user.id.in(ids));
        }
        return StreamSupport
                .stream(userRepository.findAll(booleanBuilder).spliterator(), false)
                .collect(Collectors.toMap(
                        User::getId,
                        userMapper::toUserShortDto
                ));
    }

    @Override
    @Transactional
    public void deleteBy(long userId) {
        userRepository.deleteById(userId);
    }

    public UserDto findBy(long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с Id =" + userId + " не найден"));
    }
}
