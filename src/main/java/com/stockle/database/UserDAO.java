package com.stockle.database;

import java.time.LocalDate;
import java.util.List;

import com.stockle.model.User;

public interface UserDAO {

    public void addUser(String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth);

    public void deleteUser(int id);

    public void updateUser(int id, String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth);

    public User getUserById(int id);

    public List<User> getAllUsers();


}
