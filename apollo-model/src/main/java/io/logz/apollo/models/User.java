package io.logz.apollo.models;

/**
 * Created by roiravhon on 11/20/16.
 */
public class User {

    private String userEmail;
    private String firstName;
    private String lastName;
    private String hashedPassword;
    private Boolean isAdmin;
    private Boolean isEnabled;
    private Boolean isExecAllowed;

    public User() {

    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isExecAllowed() {
        return isExecAllowed;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public void setExecAllowed(Boolean execAllowed) {
        isExecAllowed = execAllowed;
    }
}
