package graduate.schedule.repository;

import graduate.schedule.domain.store.RequiredTimeAndHeadCount;
import graduate.schedule.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface RequiredTimeAndHeadCountRepository extends JpaRepository<RequiredTimeAndHeadCount, Long> {
    Optional<RequiredTimeAndHeadCount> findByStoreAndDayOfWeek(Store store, DayOfWeek dayOfWeek);
}
