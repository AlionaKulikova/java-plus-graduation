package ewm.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ewm.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByIdIn(List<Long> ids, Pageable pageable);

    boolean existsByEmail(String email);
}