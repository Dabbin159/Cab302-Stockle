package com.stockle;

import java.util.List;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

/**
 * Manages the current user's session throughout the application
 * Handles storing the logged in user and syncing their data on logout
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private boolean darkModeEnabled = false;
    private List<com.stockle.api.data.Asset> cachedAssets; // Cache for asset data to avoid redundant API calls

    public List<com.stockle.api.data.Asset> getCachedAssets() { return cachedAssets; }
    public void setCachedAssets(List<com.stockle.api.data.Asset> assets) { this.cachedAssets = assets; }    

    private SessionManager() {}; //Prevents any external creation

    /**
     * Private Constructor prevents external instantiation
     * Use getInstance() to access the SessionManager
     * @return
     */
    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    /**
     * returns the currently logged in user
     * @return
     */
    public User getCurrentUser() { return currentUser; } // Returns current user

    /**
     * Stores the logged in user for the current session
     * @param user
     */
    public void setCurrentUser(User user) {this.currentUser = user; } // Stores who logged in

    /**
     * Checks whether a user is currently logged in
     * @return
     */
    public boolean isLoggedIn() {return currentUser != null; } //Determining whether someone is logged io or not

    /**
     * Returns whether dark mode is enabled for the current session.
     * @return true when dark mode is enabled
     */
    public boolean isDarkModeEnabled() { return darkModeEnabled; }

    /**
     * Stores the current dark mode preference for the session.
     * @param enabled whether dark mode should be enabled
     */
    public void setDarkModeEnabled(boolean enabled) { this.darkModeEnabled = enabled; }

    /**
     * Logs out the current user
     * Saves their latest data to the database before exiting the session
     */
    public void logout()
    {
        if (currentUser != null) {
            SQLUserDAO.getInstance().updateUser(currentUser); // Updates User
        }
        currentUser = null; // Turns the session off
    }
}
