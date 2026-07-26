package com.moa.backend.party.repository;

import com.moa.backend.party.entity.StudyParty;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class StudyPartySpecs {

    private StudyPartySpecs() {
    }

    public static Specification<StudyParty> withFilters(String category, String keyword, StudyParty.Status status) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(category)) {
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            return predicates;
        };
    }
}
