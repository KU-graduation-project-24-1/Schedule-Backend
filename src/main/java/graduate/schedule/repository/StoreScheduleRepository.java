package graduate.schedule.repository;

import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {
    @Query("select distinct ss.date from StoreSchedule ss " +
            "join ss.store s on s=:store " +
            "where FUNCTION('DATE_FORMAT', ss.date, '%Y-%m')=:searchMonth " +
            "order by ss.date")
    List<Date> findDatesByStoreAndMonthOrderByDate(@Param("store") Store store, @Param("searchMonth") String searchMonth);

    @EntityGraph(attributePaths = {"employee"})
    List<StoreSchedule> findSchedulesByStoreAndDate(Store store, Date date);

    @Modifying
    @Query("delete from StoreSchedule ss " +
            "where ss.store=:store")
    void deleteAllByStore(@Param("store") Store store);
}
