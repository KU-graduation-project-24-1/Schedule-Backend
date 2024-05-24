package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_name")
    private String name;

    @NotNull
    private String businessRegistrationNumber;

    @Column(length = 20)
    private String inviteCode;

    private LocalDateTime codeGeneratedTime;

    @OneToMany(mappedBy = "store", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<StoreMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<StoreSchedule> memberWorkingTimes = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<StoreAvailableSchedule> memberAvailableTimes = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<StoreAvailableTimeByDay> memberAvailableTimeByDay = new ArrayList<>();

    public static Store createStore(String storeName, String businessRegistrationNumber, String inviteCode, LocalDateTime codeGeneratedTime, Member storeCreator) {
        Store store = new Store();
        store.name = storeName;
        store.businessRegistrationNumber = businessRegistrationNumber;
        store.inviteCode = inviteCode;
        store.codeGeneratedTime = codeGeneratedTime;

        StoreMember.createBoss(store, storeCreator);

        return store;
    }

    public void addStoreMember(StoreMember storeMember) {
        this.members.add(storeMember);
    }

    public void addMemberAvailableTime(StoreAvailableSchedule memberAvailableTime) {
        this.memberAvailableTimes.add(memberAvailableTime);
    }

    public void addMemberAvailableTimeByDay(StoreAvailableTimeByDay memberAvailableTimeByDay) {
        this.memberAvailableTimeByDay.add(memberAvailableTimeByDay);
    }

    public void setNewInviteCode(String inviteCode, LocalDateTime codeGeneratedTime) {
        this.inviteCode = inviteCode;
        this.codeGeneratedTime = codeGeneratedTime;
    }
}
