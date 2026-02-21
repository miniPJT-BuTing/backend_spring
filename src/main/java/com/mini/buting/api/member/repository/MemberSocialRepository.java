package com.mini.buting.api.member.repository;

import com.mini.buting.api.member.domain.MemberSocial;
import com.mini.buting.api.member.domain.SocialProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {
    @EntityGraph(attributePaths = "member")
    Optional<MemberSocial> findByProviderNameAndProviderId(SocialProvider providerName, String providerId);

    boolean existsByEmailAndMember_IsDeletedFalse(String email);
}
