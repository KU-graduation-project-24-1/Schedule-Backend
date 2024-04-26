package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.domain.Member;
import graduate.schedule.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_FOUND_MEMBER;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public String getMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        log.info("member name : ", member.getName());

        return member.getName();
    }
}
