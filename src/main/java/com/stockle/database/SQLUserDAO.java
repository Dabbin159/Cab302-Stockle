package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    @Override
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
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    @Override
    public void deleteUser(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public User getUserById(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public java.util.List<User> getAllUsers() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
