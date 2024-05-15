package graduate.schedule.repository;

import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMember;
import graduate.schedule.dto.store.MyStoreDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {

    @Query("select case " +
            "when count(sm)> 0 then true " +
            "else false end " +
            "from StoreMember sm where sm.member=:member and sm.store=:store")
    boolean existsMember(@Param("member") Member member, @Param("store") Store store);

    @Query("select new graduate.schedule.dto.store.MyStoreDTO(s.id, s.name, sm.memberGrade) " +
            "from StoreMember sm join sm.store s " +
            "where sm.member=:member")
    List<MyStoreDTO> findStoresByMember(@Param("member") Member member);

    @Query("select case " +
            "when sm.memberGrade='BOSS' or sm.memberGrade='MANAGER' then true " +
            "else false end " +
            "from StoreMember sm where sm.member=:member and sm.store=:store")
    Boolean isExecutive(@Param("member") Member member, @Param("store") Store store);

    Optional<StoreMember> findByStoreAndMember(@Param("store") Store store, @Param("member") Member member);

    @EntityGraph(attributePaths = {"member"})
    List<StoreMember> findByStore(@Param("store") Store store);
}
