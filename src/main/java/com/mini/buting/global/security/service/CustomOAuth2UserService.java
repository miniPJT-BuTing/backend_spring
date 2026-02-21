package com.mini.buting.global.security.service;

import com.mini.buting.api.member.domain.MemberSocial;
import com.mini.buting.api.member.domain.SocialProvider;
import com.mini.buting.api.member.repository.MemberSocialRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.principal.oauth2.GuestOAuth2User;
import com.mini.buting.global.security.principal.oauth2.OAuth2UserInfo;
import com.mini.buting.global.security.principal.oauth2.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * <h2>OAuth2 사용자 정보 로드 Service</h2>
 *
 * <p>Provider로부터 사용자 정보를 조회하여,</p>
 * <ul>
 *     <li>기존 회원이면 {@link AuthUser} 반환</li>
 *     <li>최초 로그인(회원 미존재)이면 {@link com.mini.buting.global.security.principal.oauth2.GuestOAuth2User} 반환</li>
 * </ul>
 *
 * <p>최종 분기(로그인 vs 가입)는 {@link com.mini.buting.global.security.handler.oauth2.OAuth2SuccessHandlerStrategy}에서 처리</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberSocialRepository memberSocialRepository;
    private final List<OAuth2UserInfoFactory> oAuth2UserInfoFactories;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 기본 사용자 정보 로드
            OAuth2User oAuth2User = super.loadUser(userRequest);
            SocialProvider provider = SocialProvider.from(userRequest.getClientRegistration().getRegistrationId());

            // provider 별 OAuth2UserInfo 구현체 선택
            OAuth2UserInfo userInfo = oAuth2UserInfoFactories.stream()
                    .filter(factory -> factory.supports(provider))
                    .findFirst()
                    .map(factory -> factory.create(oAuth2User))
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.SOCIAL_TYPE_NOT_SUPPORTED));

            if (!StringUtils.hasText(userInfo.getProviderId())) {
                throw new BaseException(BaseResponseStatus.AUTHENTICATION_FAILED);
            }

            // 기존 소셜 가입 여부 확인
            Optional<MemberSocial> memberSocialOpt =
                    memberSocialRepository.findByProviderNameAndProviderId(provider, userInfo.getProviderId());

            // CASE 1: 기존 가입 계정인 경우 -> 로그인 진행
            if (memberSocialOpt.isPresent()) {
                var member = memberSocialOpt.get().getMember();
                if (Boolean.TRUE.equals(member.getIsDeleted())) {   // 탈퇴한 회원의 로그인 방지
                    // TODO: 클라이언트에 탈퇴한 회원 에러가 리턴되면, 재활성화하는 API로 이동
                    throw new BaseException(BaseResponseStatus.MEMBER_ALREADY_DELETED);
                }
                return AuthUser.from(member);
            }

            // CASE 2) 최초 소셜 로그인 -> 추가 회원가입 진행
            return GuestOAuth2User.of(provider, oAuth2User, userInfo);

        } catch (BaseException e) {
            throw toOAuth2Exception(e);
        } catch (Exception e) {
            log.error("{} OAuth2 loadUser unexpected error. provider={}, message={}",
                    SecurityConstants.Log.LOG_PREFIX,
                    userRequest.getClientRegistration().getRegistrationId(),
                    e.getMessage(),
                    e);
            throw toOAuth2Exception(new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * BaseException을 OAuth2AuthenticationException으로 변환
     * <p>FailureHandler에서 cause chain을 통해 BaseException의 status를 추출할 수 있도록 cause를 유지</p>
     */
    private OAuth2AuthenticationException toOAuth2Exception(BaseException e) {
        BaseResponseStatus status = e.getStatus();
        OAuth2Error error = new OAuth2Error(String.valueOf(status.getCode()), status.getMessage(), null);
        return new OAuth2AuthenticationException(error, status.getMessage(), e);
    }
}
