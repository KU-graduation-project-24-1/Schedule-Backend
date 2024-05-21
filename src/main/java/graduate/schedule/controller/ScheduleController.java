package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.SUCCESS_REQUEST_COVER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    /**
     * @apiNote 대타 요청 api
     */
    @PostMapping("/{scheduleId}/cover")
    public BaseResponse<String> requestCover(@MemberId @Valid Member member, @PathVariable @Valid Long scheduleId) {
        scheduleService.requestCover(member, scheduleId);
        return new BaseResponse<>(SUCCESS_REQUEST_COVER.getMessage());
    }

    /**
     * @apiNote 대타 수락 api
     */
    @PatchMapping("/{scheduleId}/cover")
    public BaseResponse<ChangeScheduleResponseDTO> acceptCover(@MemberId @Valid Member substitute, @PathVariable @Valid Long scheduleId) {
        ChangeScheduleResponseDTO response = scheduleService.acceptCover(substitute, scheduleId);
        return new BaseResponse<>(response);
    }
}
