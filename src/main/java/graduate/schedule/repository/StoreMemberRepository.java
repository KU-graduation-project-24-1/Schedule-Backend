package graduate.schedule.repository;

import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {

    @Query("select case " +
            "when count(sm)> 0 then true " +
            "else false end " +
            "from StoreMember sm where sm.member=:member and sm.store=:store")
    boolean existsMember(@Param("member") Member member, @Param("store") Store store);
}
