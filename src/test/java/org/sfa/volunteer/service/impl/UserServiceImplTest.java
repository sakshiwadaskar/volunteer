package org.sfa.volunteer.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sfa.volunteer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sfa.volunteer.dto.request.UserPreferenceRequest;
import org.sfa.volunteer.dto.response.UserPreferenceResponse;
import org.sfa.volunteer.exception.UserCategoryNotFoundException;
import org.sfa.volunteer.exception.UserNotFoundException;
import org.sfa.volunteer.model.UserAdditionalDetail;
import org.sfa.volunteer.model.UserCategory;
import org.sfa.volunteer.repository.UserAdditionalDetailRepository;
import org.sfa.volunteer.repository.UserCategoryRepository;
import org.sfa.volunteer.repository.UserRepository;
import java.util.Optional;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCategoryRepository userCategoryRepository;

    @Mock
    private UserAdditionalDetailRepository userAdditionalDetailRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserPreferences_Success() throws Exception {
        // Arrange
        String userId = "testUserId";
        UserCategory userCategory = UserCategory.builder()
                .userCategoryId(1)
                .userCategory("Volunteer")
                .build();
        User user = User.builder()
                .id(userId)
                .language1("English")
                .userCategory(userCategory)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAdditionalDetailRepository.findByUserId(userId)).thenReturn(null);

        // Act
        UserPreferenceResponse response = userService.getUserPreferences(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("English", response.language1());
        assertEquals("Volunteer", response.userCategory());
        verify(userRepository).findById(userId);
    }
    @Test
    void testGetUserPreferences_Success_WithExistingDetails() throws Exception {
        // Arrange
        String userId = "testUserId";

        UserCategory userCategory = UserCategory.builder()
                .userCategoryId(2)
                .userCategory("Volunteer")
                .build();

        User user = User.builder()
                .id(userId)
                .language1("English")
                .language2("Spanish")
                .userCategory(userCategory)
                .build();

        UserAdditionalDetail additionalDetail = UserAdditionalDetail.builder()
                .additionalDetailId(1L)
                .user(user)
                .secondaryEmail1("secondary@example.com")
                .secondaryPhone1("123-456-7890")
                .build();

        // Mock the repository behaviors
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAdditionalDetailRepository.findByUserId(userId)).thenReturn(additionalDetail);

        // Act
        UserPreferenceResponse response = userService.getUserPreferences(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("English", response.language1());
        assertEquals("Spanish", response.language2());
        assertEquals("Volunteer", response.userCategory());
        assertEquals("secondary@example.com", response.secondaryEmail1());
        assertEquals("123-456-7890", response.secondaryPhone1());

        // Verify interactions
        verify(userRepository).findById(userId);
        verify(userAdditionalDetailRepository).findByUserId(userId);
    }

    @Test
    void testGetUserPreferences_Success_NoAdditionalDetails() throws Exception {
        // Arrange
        String userId = "testUserId";
        User user = User.builder()
                .id(userId)
                .language1("English")
                .userCategory(null) // No category assigned yet
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Return null simulating that the user hasn't filled out extra preferences yet
        when(userAdditionalDetailRepository.findByUserId(userId)).thenReturn(null);

        // Act
        UserPreferenceResponse response = userService.getUserPreferences(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("English", response.language1());
        assertNull(response.userCategory());
        assertNull(response.secondaryEmail1());

        verify(userRepository).findById(userId);
        verify(userAdditionalDetailRepository).findByUserId(userId);
    }

    @Test
    void testGetUserPreferences_UserNotFound() {
        // Arrange
        String userId = "invalidUserId";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserPreferences(userId));

        verify(userRepository).findById(userId);
        // Ensure it stops early and never looks for additional details if user doesn't exist
        verify(userAdditionalDetailRepository, never()).findByUserId(anyString());
    }
}