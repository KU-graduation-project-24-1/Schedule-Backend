package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.response.StoreAllEmployeeResponseDTO;
import graduate.schedule.service.ExecutiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
