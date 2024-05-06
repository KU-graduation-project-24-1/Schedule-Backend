package graduate.schedule.repository;

import graduate.schedule.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query("select case " +
            "when count(s) > 0 then true " +
            "else false end " +
            "from Store s where s.inviteCode=:inviteCode")
    boolean existsInviteCode(@Param("inviteCode") String inviteCode);

    Optional<Store> findByInviteCode(@Param("inviteCode") String inviteCode);

    Optional<Store> findByBusinessRegistrationNumber(@Param("businessRegistrationNumber") String businessRegistrationNumber);

}
