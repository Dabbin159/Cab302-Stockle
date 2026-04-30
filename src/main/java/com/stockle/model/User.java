package com.stockle.model;

import java.time.LocalDate;

/**
 * Class representing a user. Contains fields for user information such as username, password, email, first name, last name, date of birth, balance, and total profit. Provides constructors for creating new users and loading existing users from the database, as well as getter and setter methods for each field.
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private long balance;
    private long totalProfit;

    /**
     * Constructor for creating a new user with default balance and total profit
     * @param username Username of the user
     * @param password Password of the user
     * @param email Email address for the user
     * @param fullName Full name of the user
     * @param dateOfBirth Data of birth of the user
     */
    public User(String username, String password, String email, String fullName, LocalDate dateOfBirth) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.balance = 100000; // Default balance for new users
        this.totalProfit = 0; // Default total profit for new users
    }

    /**
     * Constructor for creating a user with specified balance and total profit
     * @param username Username for the user
     * @param password Password for the user
     * @param email Email address for the user
     * @param fullName Full name of the user
     * @param dateOfBirth Date of birth of the user
     * @param balance Initial balance for the user
     * @param totalProfit Initial total profit for the user
     */
    public User(String username, String password, String email, String fullName, LocalDate dateOfBirth, long balance, long totalProfit) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.balance = balance;
        this.totalProfit = totalProfit;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public long getBalance() {
        return balance;
    }

    public long getTotalProfit() {
        return totalProfit;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setBalance(long balance) {
        this.balance = balance;
        
    }

    public void setTotalProfit(long totalProfit) {
        this.totalProfit = totalProfit;
    }
}