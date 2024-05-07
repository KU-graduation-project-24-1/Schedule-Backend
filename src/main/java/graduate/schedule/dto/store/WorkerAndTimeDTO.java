package graduate.schedule.dto.store;

import graduate.schedule.domain.store.StoreMemberGrade;
import lombok.Getter;

@Getter
public class WorkerAndTimeDTO {
    private Long memberWorkingTimeId;
    private Long memberId;
    private String memberName;
    private String memberGrade;
    private boolean isMine;
    private String startTime;
    private String endTime;
    private boolean isCoverRequested;

    public WorkerAndTimeDTO(Long memberWorkingTimeId, Long memberId, String memberName, StoreMemberGrade memberGrade, boolean isMine, String startTime, String endTime, boolean isCoverRequested) {
        this.memberWorkingTimeId = memberWorkingTimeId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberGrade = memberGrade.getGrade();
        this.isMine = isMine;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isCoverRequested = isCoverRequested;
    }
}
