package com.stockle.database;

import java.util.List;

import com.stockle.model.User;

public interface UserDAO {

    public void addUser(User user);

    public void deleteUser(int id);

    public void updateUser(User user);

    public User getUserById(int id);

    public List<User> getAllUsers();


}
