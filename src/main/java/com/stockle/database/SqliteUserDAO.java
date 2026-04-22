package com.stockle.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import com.stockle.model.User;

public class SqliteUserDAO implements UserDAO {

    private static SqliteUserDAO instance;
    private Connection connection;

    private SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
    }

    public static SqliteUserDAO getInstance() {
        if (instance == null) {
            instance = new SqliteUserDAO();
        }
        return instance;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }



    @Override
    public void addUser(String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteUser(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUser(int id, String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth) {
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
