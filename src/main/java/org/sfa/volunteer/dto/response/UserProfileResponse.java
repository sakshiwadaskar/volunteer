package org.sfa.volunteer.dto.response;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record UserProfileResponse(
        String id,
        String firstName,
        String middleName,
        String lastName,
        String fullName,
        String emailAddress,
        String phoneNumber,
        String timeZone,
        String profilePicturePath,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String city,
        String zipCode,
        ZonedDateTime lastUpdateDate,
        String stateName,
        String countryName,
        String userStatus,
        String userCategory,
        String lastLocation,
        String gender,
        Long language1Id,
        String language1,
        Long language2Id,
        String language2,
        Long language3Id,
        String language3,
        Integer promotionWizardStage,
        ZonedDateTime promotionWizardLastUpdateDate) {
}
