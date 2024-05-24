package graduate.schedule.repository;

import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreAvailableTimeByDay;
import graduate.schedule.domain.store.StoreMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface StoreAvailableTimeByDayRepository extends JpaRepository<StoreAvailableTimeByDay, Long> {

    @Query("select sat from StoreAvailableTimeByDay sat where sat.store = :store and sat.member = :member")
    List<StoreAvailableTimeByDay> findByStoreAndMember(@Param("store") Store store, @Param("member") Member member);

    @Query("select sat from StoreAvailableTimeByDay sat " +
            "where sat.store = :store and sat.member = :member and sat.dayOfWeek = :dayOfWeek " +
            "order by sat.startTime")
    List<StoreAvailableTimeByDay> findByStoreAndMemberAndDayOfWeekOrderByStartTime(
            @Param("store") Store store,
            @Param("member") Member member,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
