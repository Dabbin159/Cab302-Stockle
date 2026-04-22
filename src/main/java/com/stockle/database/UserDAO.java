package com.stockle.database;

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


}
