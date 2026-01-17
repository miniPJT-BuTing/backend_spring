package com.mini.buting.api.matchRequest.repository;

import com.mini.buting.api.matchRequest.domain.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
}
