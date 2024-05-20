package graduate.schedule.repository;

import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreAvailableSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public interface StoreAvailableScheduleRepository extends JpaRepository<StoreAvailableSchedule, Long> {
    @Query("select distinct sas.date from StoreAvailableSchedule sas " +
            "join sas.store s on s=:store " +
            "join sas.member m on m=:member " +
            "where FUNCTION('DATE_FORMAT', sas.date, '%Y-%m')=:searchMonth " +
            "order by sas.date")
    List<Date> findDatesByStoreAndMemberAndMonthOrderByDate(@Param("store") Store store, @Param("member") Member member, @Param("searchMonth") String searchMonth);

    List<StoreAvailableSchedule> findByStoreAndDateOrderByStartTime(Store store, Date date);

    @Modifying
    @Query("delete from StoreAvailableSchedule sas " +
            "where sas.store=:store")
    void deleteAllByStore(@Param("store") Store store);

    @Query("select distinct sas.member from StoreAvailableSchedule sas " +
            "where sas.store=:store and FUNCTION('DATE_FORMAT', sas.date, '%Y-%m')=:searchMonth " +
            "and :startTime <= sas.startTime and sas.endTime <= :endTime")
    List<Member> findMembersByStoreAndDateAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(Store store, Date searchMonth, Time startTime, Time endTime);
}
