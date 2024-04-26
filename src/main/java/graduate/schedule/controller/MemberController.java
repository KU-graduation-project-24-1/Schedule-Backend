package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/{memberId}/name")
    private BaseResponse<String> getMember(@PathVariable @Valid Long memberId) {
        String response = memberService.getMember(memberId);
        return new BaseResponse<>(response);
    }
}
