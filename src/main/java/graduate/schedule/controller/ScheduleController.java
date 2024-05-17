package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    /**
     * @apiNote 대타 수락 api
     */
    /*
    1. 대체 근무 요청자 & 대체 근무자 & 고용인에게 푸시 알림
    2. StoreMemeberWorkingTime의 member 수정(대체 근무자로) & requestCover를 false로 설정
    // 수락 누른 사람이 사장인 경우 해당 근무 정보가 삭제
    * */
}
