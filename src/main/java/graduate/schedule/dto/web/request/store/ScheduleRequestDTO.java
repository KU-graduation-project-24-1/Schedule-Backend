package graduate.schedule.dto.web.request.store;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScheduleRequestDTO {
    private List<Integer> m;  // 각 날짜별 가능한 시간대 수
    private List<Integer> k;  // 각 날짜별 필요한 전체 인원 수
    private List<List<List<Integer>>> preferences;  // 각 사람별 일자별 가능한 시간대
}