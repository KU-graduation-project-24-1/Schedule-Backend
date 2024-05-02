package graduate.schedule.domain.store;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreMemberGrade {
    EMPLOYER("사장"), EMPLOYEE("직원");

    private final String grade;

}

