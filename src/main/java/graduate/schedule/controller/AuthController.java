package graduate.schedule.controller;

import graduate.schedule.annotation.ClientIp;
import graduate.schedule.annotation.MemberId;
import graduate.schedule.annotation.RefreshToken;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.dto.web.request.auth.LoginRequestDTO;
import graduate.schedule.dto.web.response.auth.LoginResponseDTO;
import graduate.schedule.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * @apiNote 로그인 api
     * */
    @PostMapping("/login")
    public BaseResponse<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest, @ClientIp String clientIp) {
        LoginResponseDTO response = authService.kakaoLogin(loginRequest, clientIp);
        return new BaseResponse<>(response);
    }

    /**
     * @apiNote 토큰 재발급 api
     * */
    @PostMapping("/regenerate-token")
    public BaseResponse<LoginResponseDTO> regenerateToken(@MemberId @Valid Long memberId, @RefreshToken @Valid String refreshToken, @ClientIp String clientIp) {
        LoginResponseDTO response = authService.regenerateToken(memberId, refreshToken, clientIp);
        return new BaseResponse<>(response);
    }

}