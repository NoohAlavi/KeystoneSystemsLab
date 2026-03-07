package inventory.service;

import inventory.model.Role;
import inventory.model.User;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private Map<String, User> users;
    private User currentUser;

    public AuthService() {
        this.users = new HashMap<>();
        // Create default manager account
        createUser("admin", "admin123", "Administrator", Role.MANAGER);
    }

    /**
     * Create a new user account (manager only)
     */
    public boolean createUser(String username, String password, String name, Role role) {
        if (users.containsKey(username)) {
            return false; // Username already exists
        }
        users.put(username, new User(username, password, name, role));
        return true;
    }

    /**
     * Authenticate user login
     */
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Get currently logged in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user is a manager
     */
    public boolean isCurrentUserManager() {
        return currentUser != null && currentUser.isManager();
    }

    /**
     * Get all users (for manager use)
     */
    public Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }
}
