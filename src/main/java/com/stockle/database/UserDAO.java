package com.stockle.database;

import java.time.LocalDate;
import java.util.List;

import com.stockle.model.User;

public interface UserDAO {

    /**
     * Adds a new user to the database.
     * @param User The user to add.
     */
    public void addUser(User user);

    /**
     * Deletes a user from the database.
     * @param User The user to delete.
     */
    public void deleteUser(User user);

    /**
    * Updates a user's information in the database.
    * @param User The user to update.
    */
    public void updateUser(User user);

    /**
     * Retrieves a user from the database by their ID.
     * @param id The ID of the user to retrieve.
     * @return The user with the specified ID, or null if not found.
     */
    public User getUserById(int id);

    /**
     * Retrieves all users from the database.
     * @return A list of all users.
     */ 
    public List<User> getAllUsers();


    /**
     * Signs up a new user with the provided information.
     * @param username Username for the new user,
     * @param password Password for the new user,
     * @param email Email address for the new user,
     * @param firstName First name of the new user,
     * @param lastName Last name of the new user,
     * @param dateOfBirth Date of birth of the new user,
     * @param balance Initial balance for the new user,
     * @param totalProfit Initial total profit for the new user.
     * @return true if the signup was successful, false otherwise.
     */
    public boolean signup(String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth);
    

    /**
     * Logs in a user with the provided username and password.
     * @param username The username of the user trying to log in.
     * @param password The password of the user trying to log in.
     * @return The User object if login is successful, null otherwise.
     */
    public User login(String username, String password);

    /**
     * Retrieves the balance of a user by their ID.
     * @param userId The ID of the user.
     * @return The balance of the user.
     */
    public long getUserBalance(int userId);

    /**
     * Retrieves the total profit of a user by their ID.
     * @param userId The ID of the user.
     * @return The total profit of the user.
     */
    public long getUserTotalProfit(int userId);

    /**
     * Updates the balance of a user by their ID.
     * @param userId The ID of the user.
     * @param newBalance The new balance to set for the user.
     */
    public void updateUserBalance(int userId, long newBalance);

    /**
     * Updates the total profit of a user by their ID.
     * @param userId The ID of the user.
     * @param newTotalProfit The new total profit to set for the user.
     */
    public void updateUserTotalProfit(int userId, long newTotalProfit);
}
