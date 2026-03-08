package inventory.service;

import inventory.model.Role;
import inventory.model.User;
import inventory.util.CSVHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthService {
    private Map<String, User> users;
    private User currentUser;
    private static final String USERS_FILE = CSVHandler.getDataPath() + "users.csv";

    public AuthService() {
        this.users = new HashMap<>();
        loadUsersFromCSV();
        // Create default manager account if no users exist
        if (users.isEmpty()) {
            createUser("admin", "admin123", "Administrator", Role.MANAGER);
        }
    }

    /**
     * Load users from CSV file
     */
    private void loadUsersFromCSV() {
        List<String[]> data = CSVHandler.readCSV(USERS_FILE);
        // Skip header row
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length == 4) {
                User user = new User(
                    row[0], // username
                    row[1], // password
                    row[2], // name
                    Role.valueOf(row[3]) // role
                );
                users.put(user.getUsername(), user);
            }
        }
    }

    /**
     * Save all users to CSV file
     */
    private void saveUsersToCSV() {
        List<String[]> data = new ArrayList<>();
        // Add header
        data.add(new String[]{"username", "password", "name", "role"});
        // Add all users
        for (User user : users.values()) {
            data.add(new String[]{
                user.getUsername(),
                user.getPassword(),
                user.getName(),
                user.getRole().toString()
            });
        }
        CSVHandler.writeCSV(USERS_FILE, data);
    }

    /**
     * Create a new user account (manager only)
     */
    public boolean createUser(String username, String password, String name, Role role) {
        if (users.containsKey(username)) {
            return false; // Username already exists
        }
        users.put(username, new User(username, password, name, role));
        saveUsersToCSV();
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
