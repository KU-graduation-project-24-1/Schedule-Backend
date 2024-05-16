package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.ChangeWorkerRequestDTO;
import graduate.schedule.dto.web.request.ChangeWorkingTimeRequestDTO;
import graduate.schedule.dto.web.request.DeleteStoreMemberRequestDTO;
import graduate.schedule.dto.web.request.SetMemberGradeRequestDTO;
import graduate.schedule.dto.web.response.StoreAllEmployeeResponseDTO;
import graduate.schedule.service.ExecutiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/executive") /* 고용인 기능 */
public class ExecutiveController {
    private final ExecutiveService executiveService;

    /**
    * @apiNote 전체 직원 조회 api
    */
    @GetMapping("/{storeId}/employee")
    public BaseResponse<StoreAllEmployeeResponseDTO> getAllEmployees(@MemberId @Valid Member employer, @PathVariable @Valid Long storeId) {
        StoreAllEmployeeResponseDTO response = executiveService.getAllEmployees(employer, storeId);
        return new BaseResponse<>(response);
    }

    /**
     * @apiNote 고용 형태 수정 api
     */
    @PostMapping("/employee/grade")
    public BaseResponse<String> setMemberGrade(@MemberId @Valid Member employer, @RequestBody @Valid SetMemberGradeRequestDTO executiveRequest) {
        executiveService.setMemberGrade(employer, executiveRequest);
        return new BaseResponse<>(SUCCESS_SET_MEMBER_GRADE.getMessage());
    }

    /**
     * @apiNote 피고용인 삭제 api
     */
    @DeleteMapping("/employee")
    public BaseResponse<String> deleteStoreMember(@MemberId @Valid Member employer, @RequestBody @Valid DeleteStoreMemberRequestDTO executiveRequest) {
        executiveService.deleteStoreMember(employer, executiveRequest);
        return new BaseResponse<>(SUCCESS_DELETE_STORE_MEMBER.getMessage());
    }

    /**
     * @apiNote 가게 삭제 api
     */
    @DeleteMapping("/store/{storeId}")
    public BaseResponse<String> deleteStore(@MemberId @Valid Member employer, @PathVariable @Valid Long storeId) {
        executiveService.deleteStore(employer, storeId);
        return new BaseResponse<>(SUCCESS_DELETE_STORE.getMessage());
    }

    /**
     * @apiNote 근무자 수정 api
     * 대타 요청 중 스케줄 변경이(근무자, 근무 시간) 있을 시 대타 요청 사라짐
     * 대체 근무자가 사장인 경우 해당 근무 정보 삭제
     */
    @PostMapping("/schedule/employee")
    public BaseResponse<String> changeWorker(@MemberId @Valid Member employer, @RequestBody @Valid ChangeWorkerRequestDTO executiveRequest) {
        executiveService.changeWorker(employer, executiveRequest);
        return new BaseResponse<>(SUCCESS_CHANGE_WORKER.getMessage());
    }

    /**
    * @apiNote 근무 시간 수정 api
     * 대타 요청 중 스케줄 변경이(근무자, 근무 시간) 있을 시 대타 요청 사라짐
    */
    @PostMapping("/schedule/time")
    public BaseResponse<String> changeWorkingTime(@MemberId @Valid Member employer, @RequestBody @Valid ChangeWorkingTimeRequestDTO executiveRequest) {
        executiveService.changeWorkingTime(employer, executiveRequest);
        return new BaseResponse<>(SUCCESS_CHANGE_WORKING_TIME.getMessage());
    }

    /**
     * @apiNote 근무 정보 삭제 api
     * 대타 요청 중 스케줄 변경이(근무자, 근무 시간) 있을 시 대타 요청 사라짐
     */
    @DeleteMapping("/{storeId}/schedule/{scheduleId}")
    public BaseResponse<String> deleteSchedule(@MemberId @Valid Member employer, @PathVariable @Valid Long storeId, @PathVariable @Valid Long scheduleId) {
        executiveService.deleteSchedule(employer, storeId, scheduleId);
        return new BaseResponse<>(SUCCESS_DELETE_SCHEDULE.getMessage());
    }
}
