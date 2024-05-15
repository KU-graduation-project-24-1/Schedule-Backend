package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.*;
import graduate.schedule.dto.store.EmployeeDTO;
import graduate.schedule.dto.web.request.DeleteStoreMemberRequestDTO;
import graduate.schedule.dto.web.request.SetMemberGradeRequestDTO;
import graduate.schedule.dto.web.response.StoreAllEmployeeResponseDTO;
import graduate.schedule.repository.*;
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
    private final StoreMemberWorkingTimeRepository storeMemberWorkingTimeRepository;
    private final StoreMemberAvailableTimeRepository storeMemberAvailableTimeRepository;

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
        StoreMember storeMember = defaultExecutiveValidation(executiveRequest.getStoreId(), executiveRequest.getEmployeeId(), member);

        StoreMemberGrade memberGrade = StoreMemberGrade.findByGrade(executiveRequest.getMemberGrade());
        storeMember.setMemberGrade(memberGrade);
    }

    public void deleteStoreMember(Member member, DeleteStoreMemberRequestDTO executiveRequest) {
        StoreMember storeMember = defaultExecutiveValidation(executiveRequest.getStoreId(), executiveRequest.getEmployeeId(), member);
        storeMemberRepository.delete(storeMember);
    }

    private StoreMember defaultExecutiveValidation(Long storeId, Long employeeId, Member member) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        Member employee = memberRepository.findById(employeeId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        StoreMember storeMember = storeMemberRepository.findByStoreAndMember(store, employee)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER));
        if (!storeMemberRepository.isExecutive(store, member)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        return storeMember;
    }

    public void deleteStore(Member member, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.isExecutive(store, member)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        storeMemberAvailableTimeRepository.deleteAllByStore(store);
        storeMemberWorkingTimeRepository.deleteAllByStore(store);
        storeMemberRepository.deleteAllByStore(store);
        storeRepository.delete(store);
    }
}
