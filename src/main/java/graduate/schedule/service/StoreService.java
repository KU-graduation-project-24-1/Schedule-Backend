package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMember;
import graduate.schedule.dto.web.request.JoinStoreRequestDTO;
import graduate.schedule.dto.web.response.CreateStoreRequestDTO;
import graduate.schedule.dto.web.response.CreateStoreResponseDTO;
import graduate.schedule.dto.web.response.SearchStoreWithInviteCodeResponseDTO;
import graduate.schedule.repository.MemberRepository;
import graduate.schedule.repository.StoreMemberRepository;
import graduate.schedule.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final StoreMemberRepository storeMemberRepository;

    private final int LEFT_LIMIT = 48;
    private final int RIGHT_LIMIT = 122;
    static final int ASCII_NUMBER_NINE = 57;
    static final int ASCII_UPPERCASE_A = 65;
    static final int ASCII_UPPERCASE_Z = 90;
    static final int ASCII_LOWERCASE_A = 97;
    private final int TARGET_STRING_LENGTH = 8;

    public CreateStoreResponseDTO createStore(CreateStoreRequestDTO storeRequest) {
        String inviteCode = getRandomInviteCode();
        Member storeCreator = memberRepository.findById(storeRequest.getMemberId())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        Store newStore = Store.createStore(storeRequest.getStoreName(), inviteCode, storeCreator);
        storeRepository.save(newStore);

        return new CreateStoreResponseDTO(newStore);
    }

    private String getRandomInviteCode() {
        String inviteCode;
        do {
            inviteCode = generateInviteCode();
        } while (storeRepository.existsInviteCode(inviteCode));

        return inviteCode;
    }

    private String generateInviteCode() {
        Random random = new Random();

        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                .filter(i -> (i <= ASCII_NUMBER_NINE || i >= ASCII_UPPERCASE_A) && (i <= ASCII_UPPERCASE_Z || i >= ASCII_LOWERCASE_A))
                .limit(TARGET_STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public SearchStoreWithInviteCodeResponseDTO searchStoreWithInviteCode(String inviteCode) {
        Store store = storeRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new StoreException(INVALID_INVITE_CODE));
        return new SearchStoreWithInviteCodeResponseDTO(
                store.getId(),
                store.getName()
        );
    }

    public void joinStore(JoinStoreRequestDTO storeRequest) {
        Store store = storeRepository.findById(storeRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        Member member = memberRepository.findById(storeRequest.getMemberId())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        if (storeMemberRepository.existsMember(member, store)) {
            throw new StoreException(ALREADY_EXIST_STORE_MEMBER);
        }

        StoreMember.createEmployee(store, member);
    }
}
