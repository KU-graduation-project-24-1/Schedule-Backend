package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMember;
import graduate.schedule.dto.web.request.DeleteStoreMemberRequestDTO;
import graduate.schedule.dto.web.request.SetMemberGradeRequestDTO;
import graduate.schedule.dto.web.response.StoreAllEmployeeResponseDTO;
import graduate.schedule.repository.MemberRepository;
import graduate.schedule.repository.StoreMemberRepository;
import graduate.schedule.repository.StoreRepository;
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

    /**
     * @apiNote 피고용인 삭제 api
     */
    @DeleteMapping("/employee")
    public BaseResponse<String> deleteStoreMember(@MemberId @Valid Member member, @RequestBody @Valid DeleteStoreMemberRequestDTO executiveRequest) {
        executiveService.deleteStoreMember(member, executiveRequest);
        return new BaseResponse<>(SUCCESS_DELETE_STORE_MEMBER.getMessage());
    }
}
