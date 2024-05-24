package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.store.AddAvailableScheduleRequestDTO;
import graduate.schedule.dto.web.request.store.DeleteAvailableScheduleRequestDTO;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableScheduleResponseDTO;
import graduate.schedule.service.StoreScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.SUCCESS_DELETE_AVAILABLE_SCHEDULE;
import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.SUCCESS_REQUEST_COVER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store/schedule")
public class StoreScheduleController {
    private final StoreScheduleService storeScheduleService;

    /**
     * @apiNote 일 단위 근무 가능한 시간 추가 api
     */
    // TODO: 5/25/24 (추가) 전 달 8일이 이전인 경우 입력 기간 아니라고 오류 반환하기
    @PostMapping("/available")
    public BaseResponse<AddAvailableScheduleResponseDTO> addAvailableScheduleInDay(@MemberId @Valid Member member, @RequestBody @Valid AddAvailableScheduleRequestDTO storeRequest) {
        AddAvailableScheduleResponseDTO response = storeScheduleService.addAvailableScheduleInDay(member, storeRequest);
        return new BaseResponse<>(response);
    }

    /**
     * @apiNote 일 단위 근무 가능한 시간 삭제 api
     */
    @DeleteMapping("/available")
    public BaseResponse<String> deleteAvailableScheduleInDay(@MemberId @Valid Member member, @RequestBody @Valid DeleteAvailableScheduleRequestDTO storeRequest) {
        storeScheduleService.deleteAvailableScheduleInDay(member, storeRequest);
        return new BaseResponse<>(SUCCESS_DELETE_AVAILABLE_SCHEDULE.getMessage());
    }

    /**
     * @apiNote 대타 요청 api
     */
    @PostMapping("/{scheduleId}/cover")
    public BaseResponse<String> requestCover(@MemberId @Valid Member member, @PathVariable @Valid Long scheduleId) {
        storeScheduleService.requestCover(member, scheduleId);
        return new BaseResponse<>(SUCCESS_REQUEST_COVER.getMessage());
    }

    /**
     * @apiNote 대타 수락 api
     */
    @PatchMapping("/{scheduleId}/cover")
    public BaseResponse<ChangeScheduleResponseDTO> acceptCover(@MemberId @Valid Member substitute, @PathVariable @Valid Long scheduleId) {
        ChangeScheduleResponseDTO response = storeScheduleService.acceptCover(substitute, scheduleId);
        return new BaseResponse<>(response);
    }
}
