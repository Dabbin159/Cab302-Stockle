package com.stockle.model;

import java.time.LocalDate;

public class User {

    private int id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private long balance;
    private long totalProfit;

    public User(String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.balance = 100000; // Default balance for new users
        this.totalProfit = 0; // Default total profit for new users
    }

    public User(String username, String password, String email, String firstName, String lastName, LocalDate dateOfBirth, long balance, long totalProfit) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.balance = balance; // Default balance for new users
        this.totalProfit = totalProfit; // Default total profit for new users
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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