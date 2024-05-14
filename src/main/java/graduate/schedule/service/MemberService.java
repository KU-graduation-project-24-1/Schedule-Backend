package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.store.MyStoreDTO;
import graduate.schedule.dto.web.response.member.MyStoreResponseDTO;
import graduate.schedule.repository.MemberRepository;
import graduate.schedule.repository.StoreMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_FOUND_MEMBER;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final StoreMemberRepository storeMemberRepository;

    public MyStoreResponseDTO getMyStores(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        List<MyStoreDTO> myStores = storeMemberRepository.findStoresByMember(member);
        return new MyStoreResponseDTO(myStores);
    }
}
