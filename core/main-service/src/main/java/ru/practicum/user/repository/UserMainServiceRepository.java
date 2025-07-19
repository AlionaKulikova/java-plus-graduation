package ru.practicum.user.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserMainServiceRepository extends JpaRepository<User, Long> {

    @Query("SELECT u " +
            "FROM User u " +
            "WHERE u.id IN :ids")
    List<User> findAllById(List<Long> ids, Pageable pageable);

    @Query("SELECT u " +
            "FROM User u")
    List<User> findAllUser(Pageable pageable);

    Boolean existsByEmail(String email);

    List<User> findAllByIdIn(List<Long> idList, Pageable pageable);
}