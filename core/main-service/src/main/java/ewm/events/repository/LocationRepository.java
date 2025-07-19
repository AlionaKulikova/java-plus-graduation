package ewm.events.repository;

import ewm.events.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}