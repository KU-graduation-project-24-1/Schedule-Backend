package graduate.schedule.repository;

import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMemberAvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface StoreMemberAvailableTimeRepository extends JpaRepository<StoreMemberAvailableTime, Long> {
    @Query("select distinct smat.availableDate from StoreMemberAvailableTime smat " +
            "join smat.store s on s=:store " +
            "join smat.member m on m=:member " +
            "where FUNCTION('DATE_FORMAT', smat.availableDate, '%Y-%m')=:searchMonth " +
            "order by smat.availableDate")
    List<Date> findAvailableDatesByStoreAndMemberAndMonthOrderByAvailableDate(@Param("store") Store store, @Param("member") Member member, @Param("searchMonth") String searchMonth);

    List<StoreMemberAvailableTime> findAvailableSchedulesByStoreAndAvailableDateOrderByAvailableStartTime(Store store, Date date);
}
