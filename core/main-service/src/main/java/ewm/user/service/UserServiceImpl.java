package ewm.user.service;

import com.querydsl.core.BooleanBuilder;
import ewm.exception.NotFoundException;
import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
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
