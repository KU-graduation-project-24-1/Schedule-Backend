package graduate.schedule.config;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.exception.MemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_FOUND_MEMBER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberIdResolver implements HandlerMethodArgumentResolver {
    private final MemberRepository memberRepository;
    //Interceptor 이후 동작

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberId.class);
    }

    @Override
    public Member resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        return memberRepository.findById((Long) request.getAttribute("memberId"))
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }
}
