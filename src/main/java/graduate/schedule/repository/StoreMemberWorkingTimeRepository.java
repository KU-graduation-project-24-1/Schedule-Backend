package graduate.schedule.repository;

import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMemberWorkingTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface StoreMemberWorkingTimeRepository extends JpaRepository<StoreMemberWorkingTime, Long> {
    @Query("select distinct smwt.date from StoreMemberWorkingTime smwt " +
            "join smwt.store s on s=:store " +
            "where FUNCTION('DATE_FORMAT', smwt.date, '%Y-%m')=:searchMonth " +
            "order by smwt.date")
    List<Date> findDatesByStoreAndMonthOrderByDate(@Param("store") Store store, @Param("searchMonth") String searchMonth);

    @EntityGraph(attributePaths = {"member"})
    List<StoreMemberWorkingTime> findSchedulesByStoreAndDate(Store store, Date date);

    @Modifying
    @Query("delete from StoreMemberWorkingTime smwt " +
            "where smwt.store=:store")
    void deleteAllByStore(@Param("store") Store store);
}
