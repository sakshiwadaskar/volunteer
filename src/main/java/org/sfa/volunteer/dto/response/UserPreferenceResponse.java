package org.sfa.volunteer.dto.response;

import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
public record UserPreferenceResponse(
        String userId,
        Integer userCategoryId,
        String userCategory,
        String timeZone,
        Long language1Id,
        String language1,
        Long language2Id,
        String language2,
        Long language3Id,
        String language3,
        @Email String primaryEmail,
        @Email String secondaryEmail,
        String primaryPhone,
        String secondaryPhone) {
}