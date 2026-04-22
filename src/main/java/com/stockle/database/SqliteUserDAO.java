package com.stockle.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.stockle.model.User;

public class SqliteUserDAO implements UserDAO {

    private Connection connection;

    public SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    

    @Override
    public void addUser(String username, String password, String email, String firstName, String lastName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteUser(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUser(int id, String username, String password, String email, String firstName,
            String lastName) {
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
