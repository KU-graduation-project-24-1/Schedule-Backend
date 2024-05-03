package graduate.schedule.domain.store;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreMemberGrade {
    BOSS("사장"), MANAGER("매니저"), FULL_TIME("직원"), PART_TIME("아르바이트");

    private final String grade;

}

