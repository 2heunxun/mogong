package com.moa.backend.party.repository;

import com.moa.backend.party.entity.StudyParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudyPartyRepository extends JpaRepository<StudyParty, Long>, JpaSpecificationExecutor<StudyParty> {
}
