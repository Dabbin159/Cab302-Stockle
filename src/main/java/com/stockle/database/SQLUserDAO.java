package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.stockle.model.User;

public class SQLUserDAO implements UserDAO {

    private static SQLUserDAO instance;
    private Connection connection;

    private SQLUserDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

    public static SQLUserDAO getInstance() {
        if (instance == null) {
            instance = new SQLUserDAO();
        }
        return instance;
    }

    private static final String ADD_USER = "INSERT INTO users (username, password, email, firstName, lastName, dateOfBirth) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";

    private static final String UPDATE_USER = "UPDATE users SET username = ?, password = ?, email = ?, firstName = ?, lastName = ?, dateOfBirth = ? WHERE id = ?";

    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String GET_ALL_USERS = "SELECT * FROM users";

    @Override
    /**
     * @param User The user to add.
     */
    public void addUser(User user) {
           try {
            PreparedStatement statement = connection.prepareStatement(ADD_USER);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getDateOfBirth().toString());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1));
            } // Sets the generated user ID to the User
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * @param User The user to delete.
     */
    @Override
    public void deleteUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_USER);
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    /**
     * @param User The user to update.
     */
    public void updateUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_USER);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getDateOfBirth().toString());
            statement.setInt(7, user.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param id The ID of the user to retrieve.
     * @return The user with the specified ID, or null if not found.
     */
    @Override
    public User getUserById(int id) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_USER_BY_ID);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                LocalDate dateOfBirth = LocalDate.parse(resultSet.getString("dateOfBirth"));
                User user = new User(username, password, email, firstName, lastName, dateOfBirth);
                user.setId(resultSet.getInt("id"));
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
    * @return A list of all users, or null if no users are found.
    */
    @Override
    public java.util.List<User> getAllUsers() {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_ALL_USERS);
            ResultSet resultSet = statement.executeQuery();
            java.util.List<User> users = new java.util.ArrayList<>();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                LocalDate dateOfBirth = LocalDate.parse(resultSet.getString("dateOfBirth"));
                User user = new User(username, password, email, firstName, lastName, dateOfBirth);
                user.setId(resultSet.getInt("id"));
                users.add(user);
            }
            return users;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
}
