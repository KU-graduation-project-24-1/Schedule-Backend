package graduate.schedule.domain.store;

import graduate.schedule.common.exception.StoreMemberException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.INVALID_MEMBER_GRADE;

@Getter
@RequiredArgsConstructor
public enum StoreMemberGrade {
    BOSS("사장"), MANAGER("매니저"), FULL_TIME("직원"), PART_TIME("아르바이트");

    private final String grade;

    public static StoreMemberGrade findByGrade(String grade) {
        for (StoreMemberGrade memberGrade : values()) {
            if (memberGrade.grade.equals(grade)) {
                return memberGrade;
            }
        }
        throw new StoreMemberException(INVALID_MEMBER_GRADE);
    }
}

