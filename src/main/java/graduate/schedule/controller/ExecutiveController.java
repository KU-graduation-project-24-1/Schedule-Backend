package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.SetMemberGradeRequestDTO;
import graduate.schedule.dto.web.response.StoreAllEmployeeResponseDTO;
import graduate.schedule.service.ExecutiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.SUCCESS_SET_MEMBER_GRADE;

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
    public BaseResponse<StoreAllEmployeeResponseDTO> getAllEmployees(@MemberId @Valid Member member, @PathVariable @Valid Long storeId) {
        StoreAllEmployeeResponseDTO response = executiveService.getAllEmployees(member, storeId);
        return new BaseResponse<>(response);
    }

    /**
     * @apiNote 고용 형태 수정 api
     */
    @PostMapping("/employee/grade")
    public BaseResponse<String> setMemberGrade(@MemberId @Valid Member member, @RequestBody @Valid SetMemberGradeRequestDTO executiveRequest) {
        executiveService.setMemberGrade(member, executiveRequest);
        return new BaseResponse<>(SUCCESS_SET_MEMBER_GRADE.getMessage());
    }
}
