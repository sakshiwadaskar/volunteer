package org.sfa.volunteer.controller;

import org.junit.jupiter.api.Test;
import org.sfa.volunteer.dto.response.UserPreferenceResponse;
import org.sfa.volunteer.exception.UserNotFoundException;
import org.sfa.volunteer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
                .secondaryEmail1("secondary@example.com")
                .secondaryPhone1("123-456-7890")
                .build();

        when(userService.getUserPreferences(userId)).thenReturn(mockResponse);

        mockMvc.perform(get("/0.0.1/users/{userId}/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.language1").value("English"))
                .andExpect(jsonPath("$.data.language2").value("Spanish"))
                .andExpect(jsonPath("$.data.userCategory").value("Common User"))
                .andExpect(jsonPath("$.data.secondaryEmail1").value("secondary@example.com"))
                .andExpect(jsonPath("$.data.secondaryPhone1").value("123-456-7890"));
    }

    // TEST 2: Non-existent userId returns error response with success=false
    @Test
    void getPreferences_NonExistentUserId_ReturnsError() throws Exception {
        String userId = "non-existent-id";

        when(userService.getUserPreferences(userId))
                .thenThrow(new UserNotFoundException(userId));

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
                .secondaryEmail1(null)
                .secondaryPhone1(null)
                .build();

        when(userService.getUserPreferences(userId)).thenReturn(mockResponse);

        mockMvc.perform(get("/0.0.1/users/{userId}/preferences", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.language1").doesNotExist())
                .andExpect(jsonPath("$.data.secondaryEmail1").doesNotExist());
    }
}
