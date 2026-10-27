package org.sfa.volunteer.repository;

import org.junit.jupiter.api.Test;
import org.sfa.volunteer.model.User;
import org.sfa.volunteer.model.UserAdditionalDetail;
import org.sfa.volunteer.model.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserPreferencesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAdditionalDetailRepository userAdditionalDetailRepository;

    // TEST 1: findById returns correct user when user exists
    @Test
    void findById_ExistingUser_ReturnsUser() {
        // Arrange — persist required dependencies first
        UserStatus status = entityManager.persist(UserStatus.builder()
                .userStatus("Active")
                .build());

        User user = entityManager.persist(User.builder()
                .language1(1L)
                .language2(2L)
                .timeZone("America/New_York")
                .fullName("Sakshi Wadaskar")
                .userStatus(status)
                .build());

        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findById(user.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getLanguage1());
        assertEquals(2L, found.get().getLanguage2());
        assertEquals("Sakshi Wadaskar", found.get().getFullName());
    }

    // TEST 2: findById returns empty when user does not exist
    @Test
    void findById_NonExistentUser_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findById("non-existent-id");

        // Assert
        assertFalse(found.isPresent());
    }

    // TEST 3: findByUserId returns additional details when they exist
    @Test
    void findByUserId_ExistingDetails_ReturnsAdditionalDetail() {
        // Arrange
        UserStatus status = entityManager.persist(UserStatus.builder()
                .userStatus("Active")
                .build());

        User user = entityManager.persist(User.builder()
                .language1(1L)
                .timeZone("UTC")
                .fullName("Test User")
                .userStatus(status)
                .build());

        UserAdditionalDetail detail = entityManager.persist(UserAdditionalDetail.builder()
                .user(user)
                .secondaryEmail1("secondary@example.com")
                .secondaryPhone1("123-456-7890")
                .build());

        entityManager.flush();

        // Act
        UserAdditionalDetail found = userAdditionalDetailRepository.findByUserId(user.getId());

        // Assert
        assertNotNull(found);
        assertEquals("secondary@example.com", found.getSecondaryEmail1());
        assertEquals("123-456-7890", found.getSecondaryPhone1());
    }

    // TEST 4: findByUserId returns null when no additional details exist
    @Test
    void findByUserId_NoDetails_ReturnsNull() {
        // Act
        UserAdditionalDetail found = userAdditionalDetailRepository.findByUserId("no-detail-user");

        // Assert
        assertNull(found);
    }
}
