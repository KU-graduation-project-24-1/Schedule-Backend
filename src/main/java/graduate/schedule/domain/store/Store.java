package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;

//import java.time.LocalDateTime;
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

    @Column(length = 20)
    private String inviteCode;

//    private LocalDateTime codeGeneratedTime;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMemberWorkingTime> memberWorkingTimes = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreMemberAvailableTime> memberAvailableTimes = new ArrayList<>();

    public static Store createStore(String storeName, String inviteCode, Member storeCreator) {
        Store store = new Store();
        store.name = storeName;
        store.inviteCode = inviteCode;

        StoreMember.createEmployer(store, storeCreator);

        return store;
    }

    public void addStoreMember(StoreMember storeMember) {
        this.members.add(storeMember);
    }
}
