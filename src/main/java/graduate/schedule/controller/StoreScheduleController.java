package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.store.*;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableTimeByDayResponseDTO;
import graduate.schedule.dto.web.response.store.StoreScheduleResponseDTO;
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


    /**
     * @apiNote 주 단위 근무 가능한 시간 추가 api
     */
    // TODO
    @PatchMapping("/{storeId}/fixed-schedule/add")
    public BaseResponse<AddAvailableTimeByDayResponseDTO> addFixedSchedule(@MemberId @Valid Member member, @RequestBody @Valid AddStoreAvailableTimeByDayRequestDTO request) {
        AddAvailableTimeByDayResponseDTO response = storeScheduleService.addStoreAvailableTimeByDay(member, request);
        return new BaseResponse<>(response);
    }


    /**
     * @apiNote 주 단위 근무 가능한 시간 삭제 api
     */
    @PatchMapping("/{storeId}/fixed-schedule/delete")
    public BaseResponse<String> deleteFixedSchedule(@MemberId @Valid Member member, @RequestBody @Valid DeleteStoreAvailableTimeByDayRequestDTO request) {
        storeScheduleService.deleteStoreAvailableTimeByDay(member, request);
        return new BaseResponse<>(SUCCESS_DELETE_AVAILABLE_SCHEDULE.getMessage());
    }



    @PatchMapping("/{storeId}/operation-info")
    public BaseResponse<String> setStoreOperationInfo(@MemberId @Valid Member member, @PathVariable @Valid Long storeId, @RequestBody @Valid StoreOperationInfoRequestDTO request) {
        storeScheduleService.setStoreOperationInfo(member, storeId, request);
        return new BaseResponse<>("여기도 아직... 작성중입니다.");
    }

    @PostMapping("/{storeId}/generate-schedule")
    public BaseResponse<StoreScheduleResponseDTO> generateSchedule(@PathVariable Long storeId, @RequestBody ScheduleRequestDTO request) {
        StoreScheduleResponseDTO response = storeScheduleService.generateSchedule(storeId, request.getM(), request.getK(), request.getPreferences());
        return new BaseResponse<>(response);
    }
}
