package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.common.exception.StoreScheduleException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.*;
import graduate.schedule.dto.store.EmployeeDTO;
import graduate.schedule.dto.web.request.executive.ChangeScheduleRequestDTO;
import graduate.schedule.dto.web.request.executive.CreateScheduleRequestDTO;
import graduate.schedule.dto.web.request.executive.DeleteStoreMemberRequestDTO;
import graduate.schedule.dto.web.request.executive.SetMemberGradeRequestDTO;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.dto.web.response.executive.CreateScheduleResponseDTO;
import graduate.schedule.dto.web.response.executive.StoreAllEmployeeResponseDTO;
import graduate.schedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;
import static graduate.schedule.utils.DateAndTimeFormatter.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExecutiveService {
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreScheduleRepository storeScheduleRepository;
    private final StoreAvailableScheduleRepository storeAvailableScheduleRepository;

    public StoreAllEmployeeResponseDTO getAllEmployees(Member employer, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        List<StoreMember> storeMembers = storeMemberRepository.findByStore(store);
        List<EmployeeDTO> employees = storeMembers.stream()
                .map(storeMember -> new EmployeeDTO(
                        storeMember.getMemberId(),
                        storeMember.getMemberName(),
                        storeMember.getMemberGrade()
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
        Member employee = memberRepository.findById(executiveRequest.getEmployeeId())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        storeAvailableScheduleRepository.deleteAllByEmployee(employee);
        storeScheduleRepository.deleteAllByEmployee(employee);
        storeMemberRepository.delete(storeMember);
    }

    public void deleteStore(Member employer, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        storeAvailableScheduleRepository.deleteAllByStore(store);
        storeScheduleRepository.deleteAllByStore(store);
        storeMemberRepository.deleteAllByStore(store);
        storeRepository.delete(store);
    }

    public CreateScheduleResponseDTO createSchedule(Member employer, CreateScheduleRequestDTO executiveRequest) {
        Store store = storeRepository.findById(executiveRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        Member employee = memberRepository.findById(executiveRequest.getEmployeeId())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        defaultExecutiveValidation(executiveRequest.getStoreId(), executiveRequest.getEmployeeId(), employer);

        StoreSchedule newStoreSchedule = StoreSchedule.createStoreSchedule(store, employee, executiveRequest.getDate(), executiveRequest.getStartTime(), executiveRequest.getEndTime());
        storeScheduleRepository.save(newStoreSchedule);

        return new CreateScheduleResponseDTO(
                newStoreSchedule.getId(),
                newStoreSchedule.getEmployeeId(),
                newStoreSchedule.getDate(),
                timeWithoutSeconds(newStoreSchedule.getStartTime()),
                timeWithoutSeconds(newStoreSchedule.getEndTime())
        );
    }

    public ChangeScheduleResponseDTO changeSchedule(Member employer, ChangeScheduleRequestDTO executiveRequest) {
        StoreSchedule storeSchedule = storeScheduleRepository.findById(executiveRequest.getScheduleId())
                .orElseThrow(() -> new StoreScheduleException(NOT_FOUND_STORE_SCHEDULE));
        defaultExecutiveValidation(storeSchedule.getStoreId(), executiveRequest.getEmployeeId(), employer);
        Member employee = memberRepository.findById(executiveRequest.getEmployeeId()).get();

        //대체 근무자가 고용인인 경우 해당 근무 정보 삭제
        if (employee.equals(employer)) {
            log.info("대체 근무자가 고용인으로 설정되어 근무 정보를 삭제합니다.");
            storeScheduleRepository.delete(storeSchedule);
            return new ChangeScheduleResponseDTO();
        }

        storeSchedule.setEmployee(employee);
        storeSchedule.setWorkingTime(
                timeWithSeconds(executiveRequest.getStartTime()),
                timeWithSeconds(executiveRequest.getEndTime())
        );

        //대타 요청 중 스케줄 변경이(근무자, 근무 시간) 있을 시 대타 요청 사라짐
        if (storeSchedule.isRequestCover()) {
            log.info("근무 정보가 수정되어 대체 근무 요청 여부를 false로 설정합니다.");
            storeSchedule.setRequestCover(false);
        }

        return new ChangeScheduleResponseDTO(storeSchedule);
    }

    public void deleteSchedule(Member employer, Long storeId, Long scheduleId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        StoreSchedule storeSchedule = storeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StoreScheduleException(NOT_FOUND_STORE_SCHEDULE));
        if (!storeMemberRepository.isExecutive(store, employer)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        storeScheduleRepository.delete(storeSchedule);
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
