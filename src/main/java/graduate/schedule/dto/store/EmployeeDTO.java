package graduate.schedule.dto.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeDTO {
    private String name;
    private Long memberId;
    private String memberGrade;
}
