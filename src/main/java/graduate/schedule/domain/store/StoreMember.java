package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@DynamicInsert
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

    @ColumnDefault("'EMPLOYEE'")
    @Enumerated(EnumType.STRING)
    private StoreMemberGrade memberGrade;

    public static StoreMember createEmployer(Store store, Member member) {
        StoreMember storeMember = new StoreMember();
        storeMember.member = member;
        storeMember.store = store;
        storeMember.memberGrade = StoreMemberGrade.EMPLOYER;

        store.addStoreMember(storeMember);

        return storeMember;
    }
    public static StoreMember createStoreMember(Store store, Member member) {
        StoreMember storeMember = new StoreMember();
        storeMember.member = member;
        storeMember.store = store;

        store.addStoreMember(storeMember);

        return storeMember;
    }
}
