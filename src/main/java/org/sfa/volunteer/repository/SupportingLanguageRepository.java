package org.sfa.volunteer.repository;

import org.sfa.volunteer.model.SupportingLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportingLanguageRepository extends JpaRepository<SupportingLanguage, Long> {
}
