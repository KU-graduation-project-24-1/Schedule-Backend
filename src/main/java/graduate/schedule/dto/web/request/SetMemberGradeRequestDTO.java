package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class SetMemberGradeRequestDTO {
    private Long storeId;
    private Long employeeId;
    private String memberGrade;

}
