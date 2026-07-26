package com.moa.backend.party.repository;

import com.moa.backend.party.entity.PartyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    Optional<PartyMember> findByPartyIdAndUserId(Long partyId, Long userId);

    boolean existsByPartyIdAndUserId(Long partyId, Long userId);

    long countByPartyId(Long partyId);

    List<PartyMember> findByPartyIdOrderByJoinedAtAsc(Long partyId);

    void deleteByPartyIdAndUserId(Long partyId, Long userId);
}
