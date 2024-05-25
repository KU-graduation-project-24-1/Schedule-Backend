package graduate.schedule.repository;

import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreOperationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface StoreOperationInfoRepository extends JpaRepository<StoreOperationInfo, Long> {

    List<StoreOperationInfo> findByStoreAndDayOfWeek(Store store, DayOfWeek dayOfWeek);
}
