package org.sfa.volunteer.dto.response;

import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
public record UserPreferenceResponse(
        String userId,
        Integer userCategoryId,
        String userCategory,
        String language1,
        String language2,
        String language3,
        @Email String primaryEmail,
        @Email String secondaryEmail,
        String primaryPhone,
        String secondaryPhone) {
}