package org.sfa.volunteer.controller;

import org.junit.jupiter.api.Test;
import org.sfa.volunteer.dto.common.SaayamResponse;
import org.sfa.volunteer.dto.common.SaayamStatusCode;
import org.sfa.volunteer.dto.response.UserPreferenceResponse;
import org.sfa.volunteer.dto.response.UserSkillsResponse;
import org.sfa.volunteer.exception.UserNotFoundException;
import org.sfa.volunteer.model.SkillLevel;
import org.sfa.volunteer.service.ProfileImageStorageService;
import org.sfa.volunteer.service.UserService;
import org.sfa.volunteer.util.MessageSourceUtil;
import org.sfa.volunteer.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ResponseBuilder responseBuilder;

    @MockBean
    private ProfileImageStorageService profileImageStorageService;

    @MockBean
    private MessageSourceUtil messageSourceUtil;

    // TEST 1: Valid userId returns 200 with preferences data
    @Test
    void getPreferences_ValidUserId_Returns200() throws Exception {
        String userId = "test-user-id-123";

        UserPreferenceResponse mockResponse = UserPreferenceResponse.builder()
                .userId(userId)
                .userCategoryId(1)
                .userCategory("Common User")
                .language1("English")
                .language2("Spanish")
                .primaryEmail("secondary@example.com")
                .primaryPhone("123-456-7890")
                .build();

        when(userService.getUserPreferences(userId)).thenReturn(mockResponse);
        when(responseBuilder.buildSuccessResponse(any(), any(), any()))
                .thenReturn(SaayamResponse.success(SaayamStatusCode.SUCCESS, "Success", mockResponse));

        mockMvc.perform(get("/0.0.1/users/{userId}/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.language1").value("English"))
                .andExpect(jsonPath("$.data.language2").value("Spanish"))
                .andExpect(jsonPath("$.data.userCategory").value("Common User"))
                .andExpect(jsonPath("$.data.primaryEmail").value("secondary@example.com"))
                .andExpect(jsonPath("$.data.primaryPhone").value("123-456-7890"));
    }

    // TEST 2: Non-existent userId returns error response with success=false
    @Test
    void getPreferences_NonExistentUserId_ReturnsError() throws Exception {
        String userId = "non-existent-id";

        when(userService.getUserPreferences(userId))
                .thenThrow(new UserNotFoundException(userId));
        when(responseBuilder.buildErrorResponse(eq(404), eq(SaayamStatusCode.USER_NOT_FOUND), any()))
                .thenReturn(SaayamResponse.error(404, SaayamStatusCode.USER_NOT_FOUND, "User not found"));

        mockMvc.perform(get("/0.0.1/users/{userId}/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    // TEST 3: Valid userId with no additional details returns 200 with nulls
    @Test
    void getPreferences_ValidUserId_NoAdditionalDetails_Returns200() throws Exception {
        String userId = "test-user-id-456";

        UserPreferenceResponse mockResponse = UserPreferenceResponse.builder()
                .userId(userId)
                .userCategoryId(null)
                .userCategory(null)
                .language1(null)
                .language2(null)
                .primaryEmail(null)
                .primaryPhone(null)
                .build();

        when(userService.getUserPreferences(userId)).thenReturn(mockResponse);
        when(responseBuilder.buildSuccessResponse(any(), any(), any()))
                .thenReturn(SaayamResponse.success(SaayamStatusCode.SUCCESS, "Success", mockResponse));

        mockMvc.perform(get("/0.0.1/users/{userId}/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.language1").doesNotExist())
                .andExpect(jsonPath("$.data.primaryEmail").doesNotExist());
    }

    @Test
    void shouldReturnUserSkills() throws Exception {

        String requestJson = """
        {
          "userId": "123"
        }
        """;

        UserSkillsResponse serviceResponse = new UserSkillsResponse(
                "123", List.of("cat.1"), Map.of("cat.1", SkillLevel.ADVANCED));

        when(userService.getUserSkills("123"))
                .thenReturn(serviceResponse);

        when(responseBuilder.buildSuccessResponse(any(), any(), any()))
                .thenReturn(
                        SaayamResponse.success(
                                SaayamStatusCode.SUCCESS,
                                "Success",
                                serviceResponse
                        )
                );

        mockMvc.perform(post("/0.0.1/users/profileSkills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.skills[0]").value("cat.1"))
                .andExpect(jsonPath("$.data.skillLevels['cat.1']").value("ADVANCED"));

        verify(userService).getUserSkills("123");
    }

    @Test
    void shouldFailWhenUserIdMissing() throws Exception {

        String requestJson = "{}";

        // GlobalExceptionHandler reports validation errors in the SaayamResponse body
        mockMvc.perform(post("/0.0.1/users/profileSkills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(responseBuilder).buildErrorResponse(
                eq(HttpStatus.BAD_REQUEST.value()), eq(SaayamStatusCode.BAD_REQUEST), anyString());
        verify(userService, never()).getUserSkills(any());
    }
}
