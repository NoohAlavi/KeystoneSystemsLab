package inventory.model;

public class User {
    private String username;
    private String password;
    private String name;
    private Role role;

    public User(String username, String password, String name, Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isEmployee() {
        return role == Role.EMPLOYEE;
    }
}
