package graduate.schedule.dto.store;

import graduate.schedule.domain.store.StoreMemberGrade;
import lombok.Getter;

@Getter
public class EmployeeDTO {
    private Long memberId;
    private String name;
    private String memberGrade;

    public EmployeeDTO(Long memberId, String name, StoreMemberGrade memberGrade) {
        this.memberId = memberId;
        this.name = name;
        this.memberGrade = memberGrade.getGrade();
    }
}
