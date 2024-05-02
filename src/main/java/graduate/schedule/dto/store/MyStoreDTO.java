package graduate.schedule.dto.store;

import graduate.schedule.domain.store.StoreMemberGrade;
import lombok.Getter;

@Getter
public class MyStoreDTO {
    private Long storeId;
    private String storeName;
    private String grade;

    public MyStoreDTO(Long storeId, String storeName, StoreMemberGrade memberGrade) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.grade = memberGrade.getGrade();
    }
}
