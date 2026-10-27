package org.sfa.volunteer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "supporting_languages")
public class SupportingLanguage {

    @Id
    @Column(name = "language_id", nullable = false)
    private Long languageId;

    @Column(name = "language_name", nullable = false)
    private String languageName;

    @Column(name = "iso_code", nullable = false)
    private String isoCode;

    @Column(name = "locale_code", nullable = false)
    private String localeCode;

    @Column(name = "writing_direction")
    private String writingDirection;

    @Column(name = "is_active")
    private Boolean isActive;
}
