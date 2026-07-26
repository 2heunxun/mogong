package com.moa.backend.party;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudyPartyRepository extends JpaRepository<StudyParty, Long>, JpaSpecificationExecutor<StudyParty> {
}
