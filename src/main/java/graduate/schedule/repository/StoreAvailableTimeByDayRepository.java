package graduate.schedule.repository;

import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreAvailableTimeByDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface StoreAvailableTimeByDayRepository extends JpaRepository<StoreAvailableTimeByDay, Long> {
    List<StoreAvailableTimeByDay> findByStoreAndMember(Store store, Member member);
    Optional<StoreAvailableTimeByDay> findByStoreAndMemberAndDayOfWeek(@Param("store") Store store, @Param("member") Member member, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
