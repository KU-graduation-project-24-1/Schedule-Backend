package graduate.schedule.controller;

import graduate.schedule.annotation.ClientIp;
import graduate.schedule.annotation.MemberId;
import graduate.schedule.annotation.RefreshToken;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.auth.LoginRequestDTO;
import graduate.schedule.dto.web.request.auth.SetMemberNameRequestDTO;
import graduate.schedule.dto.web.response.auth.LoginResponseDTO;
import graduate.schedule.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.SUCCESS_SAVE_MEMBER_NAME;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * @apiNote (카카오) 로그인 api
     * */
    @PostMapping("/login")
    public BaseResponse<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO authRequest, @ClientIp String clientIp) {
        LoginResponseDTO response = authService.kakaoLogin(authRequest, clientIp);
        return new BaseResponse<>(response);
    }

    /**
     * @apiNote 토큰 재발급 api
     * */
    @PostMapping("/regenerate-token")
    public BaseResponse<LoginResponseDTO> regenerateToken(@MemberId @Valid Member member, @RefreshToken @Valid String refreshToken, @ClientIp String clientIp) {
        LoginResponseDTO response = authService.regenerateToken(member, refreshToken, clientIp);
        return new BaseResponse<>(response);
    }

    /**
    * @apiNote 회원가입시 사용자 이름 설정 api
    */
    @PostMapping("/name")
    public BaseResponse<String> setMemberName(@MemberId @Valid Member member, @RequestBody @Valid SetMemberNameRequestDTO authRequest) {
        authService.setMemberName(member, authRequest);
        return new BaseResponse<>(SUCCESS_SAVE_MEMBER_NAME.getMessage());
    }

}