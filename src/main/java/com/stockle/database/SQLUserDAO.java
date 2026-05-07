package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.stockle.model.User;


/**
 * Class to manage user-related database operations, such as adding, deleting, updating, and retrieving users. Implements the UserDAO interface.
 */
public class SQLUserDAO implements UserDAO {

    private static SQLUserDAO instance;
    private final Connection connection;

    private SQLUserDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

    /**
     * Returns the singleton instance of SQLUserDAO
     * @return the singleton instance of SQLUserDAO
     */
    public static SQLUserDAO getInstance() {
        if (instance == null) {
            instance = new SQLUserDAO();
        }
        return instance;
    }

    private static final String ADD_USER = "INSERT INTO users (username, password, email, fullName, dateOfBirth, balance, totalProfit) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";

    private static final String UPDATE_USER = "UPDATE users SET username = ?, password = ?, email = ?, fullName = ?, dateOfBirth = ?, balance = ?, totalProfit = ? WHERE id = ?";

    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String GET_ALL_USERS = "SELECT * FROM users";

    private static final String LOGIN_USER = "SELECT * FROM users WHERE email = ?";

    private static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE email = ?";

    private static final String GET_BALANCE_BYID = "SELECT balance FROM users WHERE id = ?";

    private static final String GET_PROFIT_BYID = "SELECT totalProfit FROM users WHERE id = ?";

    private static final String UPDATE_BALANCE_BYID = "UPDATE users SET balance = ? WHERE id = ?";

    private static final String UPDATE_PROFIT_BYID = "UPDATE users SET totalProfit = ? WHERE id = ?";

    @Override
    /**
     * Adds a new user to the database.
     * @param User The user to add.
     */
    public void addUser(User user) {
           try {
            PreparedStatement statement = connection.prepareStatement(ADD_USER, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getFullName());
            statement.setString(5, user.getDateOfBirth().toString());
            statement.setLong(6, user.getBalance());
            statement.setLong(7, user.getTotalProfit());
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
     * Deletes a user from the database.
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
    * Updates a user's information in the database.
    * @param User The user to update.
    */
    public void updateUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_USER);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getFullName());
            statement.setString(5, user.getDateOfBirth().toString());
            statement.setLong(6, user.getBalance());
            statement.setLong(7, user.getTotalProfit());
            statement.setInt(8, user.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves a user from the database by their ID.
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
                String fullName = resultSet.getString("fullName");
                LocalDate dateOfBirth = LocalDate.parse(resultSet.getString("dateOfBirth"));
                long balance = resultSet.getLong("balance");
                long totalProfit = resultSet.getLong("totalProfit");
                User user = new User(username, password, email, fullName, dateOfBirth, balance, totalProfit);
                user.setId(resultSet.getInt("id"));
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all users from the database.
     * @return A list of all users.
     */ 
    @Override
    public List<User> getAllUsers() {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_ALL_USERS);
            ResultSet resultSet = statement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String fullName = resultSet.getString("fullName");
                LocalDate dateOfBirth = LocalDate.parse(resultSet.getString("dateOfBirth"));
                User user = new User(username, password, email, fullName, dateOfBirth);
                user.setId(resultSet.getInt("id"));
                users.add(user);
            }
            return users;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * Signs up a new user with the provided information.
     * @param username Username for the new user,
     * @param password Password for the new user,
     * @param email Email address for the new user,
     * @param fullName Full name of the new user,
     * @param dateOfBirth Date of birth of the new user,
     * @return true if the signup was successful, false otherwise.
     */
    @Override
    public boolean signup(String username, String password, String email, String fullName, LocalDate dateOfBirth) {
        // Implementation for signing up a new user
        try {
            String hashed_password = PasswordUtils.hashPassword(password);
            User user = new User(username, hashed_password, email, fullName, dateOfBirth);
            addUser(user);
            return true;
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Logs in a user with the provided email and password.
     * @param email The email of the user trying to log in.
     * @param password The password of the user trying to log in.
     * @return The User object if login is successful, null otherwise.
     */
    @Override
    public User login(String email, String password) {
        try (
            PreparedStatement statement = connection.prepareStatement(LOGIN_USER)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedHash = resultSet.getString("password");
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    String usernameDB = resultSet.getString("username");
                    String emailDB = resultSet.getString("email");
                    String fullName = resultSet.getString("fullName");
                    LocalDate dob = LocalDate.parse(resultSet.getString("dateOfBirth"));
                    long balance = resultSet.getLong("balance");
                    long totalProfit = resultSet.getLong("totalProfit");
                    User user = new User(usernameDB, storedHash, emailDB, fullName, dob, balance, totalProfit);
                    user.setId(resultSet.getInt("id"));
                    return user;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a user from the database by their email address.
     * Used primarily for test cleanup to ensure a fresh state before each test case.
     * @param email
     */
    public void deleteUserByEmail(String email) {
        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_USER_BY_EMAIL);
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
    * @param userId The ID of the user.
    * @return The balance of the user, or 0 if an error occurs.
    */
     * Retrieves the balance of a user by their ID.
     * @param userId The ID of the user.
     * @return The balance of the user.
     */
    @Override
    public long getUserBalance(int userId) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_BALANCE_BYID);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("balance");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Retrieves the total profit of a user by their ID.
     * @param userId The ID of the user.
     * @return The total profit of the user.
     */
    @Override
    public long getUserTotalProfit(int userId) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_PROFIT_BYID);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("totalProfit");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Updates the balance of a user by their ID.
     * @param userId The ID of the user.
     * @param newBalance The new balance to set for the user.
     */
    @Override
    public void updateUserBalance(int userId, long newBalance) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_BALANCE_BYID);
            statement.setLong(1, newBalance);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Updates the total profit of a user by their ID.
     * @param userId The ID of the user.
     * @param newTotalProfit The new total profit to set for the user.
     */
    @Override
    public void updateUserTotalProfit(int userId, long newTotalProfit) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_PROFIT_BYID);
            statement.setLong(1, newTotalProfit);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}