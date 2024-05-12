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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_name")
    private String name;

    @NotNull
    private String businessRegistrationNumber;

    @Column(length = 20)
    private String inviteCode;

    private LocalDateTime codeGeneratedTime;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMemberWorkingTime> memberWorkingTimes = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMemberAvailableTime> memberAvailableTimes = new ArrayList<>();

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
    public void addMemberAvailableTime(StoreMemberAvailableTime memberAvailableTime) {
        this.memberAvailableTimes.add(memberAvailableTime);
    }

    public void setNewInviteCode(String inviteCode, LocalDateTime codeGeneratedTime) {
        this.inviteCode = inviteCode;
        this.codeGeneratedTime = codeGeneratedTime;
    }
}
