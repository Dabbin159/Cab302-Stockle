package com.stockle;

import com.stockle.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}; //Prevents any external creation

    // Only one Session going at a time
    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() { return currentUser; } // Returns current user
    public void setCurrentUser(User user) {this.currentUser = user; } // Stores who logged in
    public boolean isLoggedIn() {return currentUser != null; } //Determining whether someone is logged io or not
    public void logout() {currentUser = null; } // Turns the session off
}
