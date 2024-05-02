package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import static graduate.schedule.domain.store.StoreMemberGrade.*;

@Entity
@Getter
public class StoreMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private StoreMemberGrade memberGrade;

    public static StoreMember createEmployer(Store store, Member member) {
        StoreMember storeMember = new StoreMember();
        storeMember.member = member;
        storeMember.store = store;
        storeMember.memberGrade = EMPLOYER;

        store.addStoreMember(storeMember);

        return storeMember;
    }
    public static StoreMember createEmployee(Store store, Member member) {
        StoreMember storeMember = new StoreMember();
        storeMember.member = member;
        storeMember.store = store;
        storeMember.memberGrade = EMPLOYEE;

        store.addStoreMember(storeMember);

        return storeMember;
    }
}
