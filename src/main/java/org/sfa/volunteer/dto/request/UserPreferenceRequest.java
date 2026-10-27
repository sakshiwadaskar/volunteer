package org.sfa.volunteer.dto.request;
import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
public record UserPreferenceRequest(
        Integer userCategoryId,
        Long language1,
        Long language2,
        Long language3,
        @Email String secondaryEmail1,
        @Email String secondaryEmail2,
        String secondaryPhone1,
        String secondaryPhone2) {
}