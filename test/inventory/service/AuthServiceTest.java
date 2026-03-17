package inventory.service;

import inventory.model.Role;
import inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService();
    }

    @AfterEach
    void tearDown() {
        // Delete all test users from the map
        String[] testUsernames = {
                "TESTUSER00000000001",
                "TESTUSER00000000002",
                "TESTUSER00000000003",
                "TESTUSER00000000004",
                "TESTUSER00000000005"
        };

        try {
            // Access private 'users' map
            java.lang.reflect.Field usersField = AuthService.class.getDeclaredField("users");
            usersField.setAccessible(true);
            Map<String, User> usersMap = (Map<String, User>) usersField.get(service);

            for (String username : testUsernames) {
                usersMap.remove(username);
            }

            // Save changes back to CSV
            java.lang.reflect.Method saveMethod = AuthService.class.getDeclaredMethod("saveUsersToCSV");
            saveMethod.setAccessible(true);
            saveMethod.invoke(service);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateUserSuccess() {
        boolean result = service.createUser(
                "TESTUSER00000000001", "password123", "Test User", Role.EMPLOYEE);
        assertTrue(result);
        assertNotNull(service.getAllUsers().get("TESTUSER00000000001"));
    }

    @Test
    void testCreateUserDuplicate() {
        service.createUser("TESTUSER00000000002", "pass", "User2", Role.EMPLOYEE);
        boolean result = service.createUser("TESTUSER00000000002", "pass2", "User3", Role.MANAGER);
        assertFalse(result);
    }

    @Test
    void testLoginSuccess() {
        service.createUser("TESTUSER00000000003", "mypassword", "LoginUser", Role.EMPLOYEE);
        boolean result = service.login("TESTUSER00000000003", "mypassword");
        assertTrue(result);
        assertEquals("TESTUSER00000000003", service.getCurrentUser().getUsername());
    }

    @Test
    void testLoginFailWrongPassword() {
        service.createUser("TESTUSER00000000004", "pw123", "UserFail", Role.EMPLOYEE);
        boolean result = service.login("TESTUSER00000000004", "wrongpw");
        assertFalse(result);
        assertNull(service.getCurrentUser());
    }

    @Test
    void testLogoutAndIsLoggedIn() {
        service.createUser("TESTUSER00000000005", "pw5", "User5", Role.EMPLOYEE);
        service.login("TESTUSER00000000005", "pw5");
        assertTrue(service.isLoggedIn());

        service.logout();
        assertFalse(service.isLoggedIn());
        assertNull(service.getCurrentUser());
    }

    @Test
    void testIsCurrentUserManager() {
        service.createUser("TESTUSER00000000001", "pass", "ManagerTest", Role.MANAGER);
        service.login("TESTUSER00000000001", "pass");
        assertTrue(service.isCurrentUserManager());

        service.logout();
        service.createUser("TESTUSER00000000002", "pass", "EmployeeTest", Role.EMPLOYEE);
        service.login("TESTUSER00000000002", "pass");
        assertFalse(service.isCurrentUserManager());
    }
}