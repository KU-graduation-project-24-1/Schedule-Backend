package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.common.exception.StoreScheduleException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.*;
import graduate.schedule.dto.store.EmployeeDTO;
import graduate.schedule.dto.web.request.ChangeWorkerRequestDTO;
import graduate.schedule.dto.web.request.ChangeWorkingTimeRequestDTO;
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
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithSeconds;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExecutiveService {
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreScheduleRepository storeScheduleRepository;
    private final StoreMemberAvailableTimeRepository storeMemberAvailableTimeRepository;

    public StoreAllEmployeeResponseDTO getAllEmployees(Member employer, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        List<StoreMember> storeMembers = storeMemberRepository.findByStore(store);
        List<EmployeeDTO> employees = storeMembers.stream()
                .map(storeMember -> new EmployeeDTO(
                        storeMember.getMember().getName(),
                        storeMember.getMember().getId(),
                        storeMember.getMemberGrade().getGrade()
                )).toList();
        return new StoreAllEmployeeResponseDTO(employees);
    }

    public void setMemberGrade(Member employer, SetMemberGradeRequestDTO executiveRequest) {
        StoreMember storeMember = defaultExecutiveValidation(executiveRequest.getStoreId(), executiveRequest.getEmployeeId(), employer);

        StoreMemberGrade memberGrade = StoreMemberGrade.findByGrade(executiveRequest.getMemberGrade());
        storeMember.setMemberGrade(memberGrade);
    }

    public void deleteStoreMember(Member employer, DeleteStoreMemberRequestDTO executiveRequest) {
        StoreMember storeMember = defaultExecutiveValidation(executiveRequest.getStoreId(), executiveRequest.getEmployeeId(), employer);
        storeMemberRepository.delete(storeMember);
    }

    public void deleteStore(Member employer, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        storeMemberAvailableTimeRepository.deleteAllByStore(store);
        storeScheduleRepository.deleteAllByStore(store);
        storeMemberRepository.deleteAllByStore(store);
        storeRepository.delete(store);
    }

    public void changeWorker(Member employer, ChangeWorkerRequestDTO executiveRequest) {
        StoreSchedule storeSchedule = storeScheduleRepository.findById(executiveRequest.getScheduleId())
                .orElseThrow(() -> new StoreScheduleException(INVALID_STORE_SCHEDULE_ID));
        defaultExecutiveValidation(storeSchedule.getStore().getId(), executiveRequest.getEmployeeId(), employer);
        Member employee = memberRepository.findById(executiveRequest.getEmployeeId()).get();


        //대체 근무자가 사장인 경우 해당 근무 정보 삭제
        if (employee.equals(employer)) {
            log.info("대체 근무자가 고용인으로 설정되어 근무 정보를 삭제합니다.");
            storeScheduleRepository.delete(storeSchedule);
            return;
        }

        storeSchedule.setMember(employee);
        //대타 요청 중 스케줄 변경이(근무자, 근무 시간) 있을 시 대타 요청 사라짐
        if (storeSchedule.isRequestCover()) {
            log.info("근무 정보가 수정되어 대체 근무 요청 여부를 false로 설정합니다.");
            storeSchedule.setRequestCover(false);
        }
    }

    public void changeWorkingTime(Member employer, ChangeWorkingTimeRequestDTO executiveRequest) {
        StoreSchedule storeSchedule = storeScheduleRepository.findById(executiveRequest.getScheduleId())
                .orElseThrow(() -> new StoreScheduleException(INVALID_STORE_SCHEDULE_ID));
        Store store = storeRepository.findById(storeSchedule.getStore().getId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        storeSchedule.setWorkingTime(
                timeWithSeconds(executiveRequest.getStartTime()),
                timeWithSeconds(executiveRequest.getEndTime())
        );
        //대타 요청 중 스케줄 변경이(근무자, 근무 시간) 있을 시 대타 요청 사라짐
        if (storeSchedule.isRequestCover()) {
            log.info("근무 정보가 수정되어 대체 근무 요청 여부를 false로 설정합니다.");
            storeSchedule.setRequestCover(false);
        }
    }

    private StoreMember defaultExecutiveValidation(Long storeId, Long employeeId, Member employer) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        Member employee = memberRepository.findById(employeeId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        StoreMember storeMember = storeMemberRepository.findByStoreAndMember(store, employee)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        return storeMember;
    }
}
