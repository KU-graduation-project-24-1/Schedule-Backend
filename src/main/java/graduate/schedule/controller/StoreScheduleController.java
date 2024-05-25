package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.store.*;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableTimeByDayResponseDTO;
import graduate.schedule.dto.web.response.store.AddStoreOperationInfoResponseDTO;
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
    @DeleteMapping("/{storeId}/fixed-schedule/delete")
    public BaseResponse<String> deleteFixedSchedule(@MemberId @Valid Member member, @PathVariable Long storeId,@RequestBody @Valid DeleteStoreAvailableTimeByDayRequestDTO request) {
        storeScheduleService.deleteStoreAvailableTimeByDay(member, request);
        return new BaseResponse<>(SUCCESS_DELETE_AVAILABLE_SCHEDULE.getMessage());
    }



    @PatchMapping("/{storeId}/operation-info")
    public BaseResponse<AddStoreOperationInfoResponseDTO> addStoreOperationInfo(
            @PathVariable Long storeId,
            @RequestBody @Valid StoreOperationInfoRequestDTO request,
            @MemberId Member member) {
        AddStoreOperationInfoResponseDTO response = storeScheduleService.addStoreOperationInfo(member, storeId, request);
        return new BaseResponse<>(response);
    }

    @DeleteMapping("/{storeId}/operation-info")
    public BaseResponse<String> deleteStoreOperationInfo(
            @RequestBody @Valid DeleteStoreOperationInfoRequestDTO request,
            @MemberId Member member) {
        storeScheduleService.deleteStoreOperationInfo(member, request);
        return new BaseResponse<>("운영 정보가 삭제되었습니다.");
    }

    @PostMapping("/{storeId}/generate-schedule")
    public BaseResponse<StoreScheduleResponseDTO> generateSchedule(@PathVariable Long storeId, @RequestBody ScheduleRequestDTO request) {
        StoreScheduleResponseDTO response = storeScheduleService.generateSchedule(storeId, request.getM(), request.getK(), request.getPreferences());
        return new BaseResponse<>(response);
    }
}
