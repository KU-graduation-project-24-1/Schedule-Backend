package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMember;
import graduate.schedule.domain.store.StoreMemberGrade;
import graduate.schedule.dto.store.EmployeeDTO;
import graduate.schedule.dto.web.request.SetMemberGradeRequestDTO;
import graduate.schedule.dto.web.response.StoreAllEmployeeResponseDTO;
import graduate.schedule.repository.MemberRepository;
import graduate.schedule.repository.StoreMemberRepository;
import graduate.schedule.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExecutiveService {
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final StoreMemberRepository storeMemberRepository;

    public StoreAllEmployeeResponseDTO getAllEmployees(Member member, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        List<StoreMember> storeMembers = storeMemberRepository.findByStore(store);
        List<EmployeeDTO> employees = storeMembers.stream()
                .map(storeMember -> new EmployeeDTO(
                        storeMember.getMember().getName(),
                        storeMember.getMember().getId(),
                        storeMember.getMemberGrade().getGrade()
                )).toList();
        return new StoreAllEmployeeResponseDTO(employees);
    }

    public void setMemberGrade(Member member, SetMemberGradeRequestDTO executiveRequest) {
        Store store = storeRepository.findById(executiveRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        Member employee = memberRepository.findById(executiveRequest.getEmployeeId())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        StoreMember storeMember = storeMemberRepository.findByStoreAndMember(store, employee)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER));
        if (!storeMemberRepository.isExecutive(member, store)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        StoreMemberGrade memberGrade = StoreMemberGrade.findByGrade(executiveRequest.getMemberGrade());
        storeMember.setMemberGrade(memberGrade);
    }
}
