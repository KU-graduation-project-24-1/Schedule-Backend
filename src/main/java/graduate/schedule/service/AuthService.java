package graduate.schedule.service;

import graduate.schedule.auth.kakao.KakaoMemberProvider;
import graduate.schedule.common.exception.InvalidTokenException;
import graduate.schedule.common.exception.MemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.auth.KakaoMemberDTO;
import graduate.schedule.dto.web.request.auth.LoginRequestDTO;
import graduate.schedule.dto.web.response.auth.LoginResponseDTO;
import graduate.schedule.repository.MemberRepository;
import graduate.schedule.utils.auth.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final KakaoMemberProvider kakaoMemberProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public LoginResponseDTO kakaoLogin(@Valid LoginRequestDTO loginRequest, String clientIp) {
        KakaoMemberDTO kakaoPlatformMember = kakaoMemberProvider.getPayloadFromIdToken(loginRequest.getIdToken());
        return generateLoginResponse(kakaoPlatformMember.getEmail(), kakaoPlatformMember.getProfileImg(), kakaoPlatformMember.getPlatformId(), loginRequest.getFcmToken(), clientIp);
    }

    private LoginResponseDTO generateLoginResponse(String email, String profileImg, String platformId, String fcmToken, String clientIp) {
        Optional<Member> findMember = memberRepository.findByPlatformId(platformId);

        //회원가입이 되어 있는 멤버
        return findMember.map(member -> getLoginResponse(member, clientIp, true))
                //회원가입이 필요한 멤버
                .orElseGet(() -> signUp(email, profileImg, platformId, clientIp, fcmToken));
    }

    private LoginResponseDTO signUp(String email, String profileImg, String platformId, String clientIp, String fcmToken) {
        // 회원가입이 필요한 멤버
        Member signUpMember = Member.createMember(email, profileImg, platformId, fcmToken);
        memberRepository.save(signUpMember);
        log.info("오늘 알바 회원 가입");

        return getLoginResponse(signUpMember, clientIp, false);
    }

    private LoginResponseDTO getLoginResponse(Member targetMember, String clientIp, boolean isRegisteredBefore) {
        String accessToken = jwtTokenProvider.createAccessToken(targetMember.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(targetMember.getId());

        // Redis 에 refresh token 저장
        redisTemplate.opsForValue().set(refreshToken, clientIp);
        log.info("오늘 알바에 로그인하였습니다.");

        return new LoginResponseDTO(targetMember.getEmail(), accessToken, refreshToken, targetMember.getProfileImg(), isRegisteredBefore);
    }

    public LoginResponseDTO regenerateToken(Long memberId, String refreshToken, String clientIp) {
        // Redis 에서 해당 refresh token 찾기
        String existingIp = redisTemplate.opsForValue().get(refreshToken);

        // 찾은 값의 validation 처리
        validateRefreshTokenExisting(existingIp);
        compareClientIpFromRedis(existingIp, clientIp);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        return getLoginResponse(member, clientIp, true);
    }

    private void validateRefreshTokenExisting(String existingIp) {
        if (existingIp == null) {
            log.error(INVALID_REFRESH_TOKEN.getMessage());
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN);
        }
    }

    private void compareClientIpFromRedis(String existingIp, String clientIp) {
        if (!existingIp.equals(clientIp)) {
            log.error(IP_MISMATCH.getMessage());
            throw new InvalidTokenException(IP_MISMATCH);
        }
    }
}
