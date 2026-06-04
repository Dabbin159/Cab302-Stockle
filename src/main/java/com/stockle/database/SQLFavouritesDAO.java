package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLFavouritesDAO implements FavouritesDAO {

    private static SQLFavouritesDAO instance;
    private Connection connection;

    // Private constructor - use getInstance() to get the DAO
    private SQLFavouritesDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

    // Singleton - only one instance of this DAO needed
    public static SQLFavouritesDAO getInstance() {
        if (instance == null) {
            instance = new SQLFavouritesDAO();
        }
        return instance;
    }

    // Inserts a new row or updates the existing one if userID already exists
    private static final String INSERT_UPDATE_FAVOURITES = "INSERT INTO favourites (userID, favouritesList) VALUES " +
            "(?, ?)" + "ON CONFLICT(userID) DO UPDATE SET favouritesList = excluded.favouritesList";
    private static final String GET_FAVOURITES = "SELECT favouritesList FROM favourites where userID = ?";

    // Wipes the whole favourites row for a user - used for test cleanup
    private static final String DELETE_USER_FAVOURITES = "DELETE FROM FAVOURITES WHERE userID = ?";

    @Override
    // Adds stocks to the user's favourites list, skips any that are already in there
    public void addFavourite(int userID, List<String> favourites) {
        try {
            List<String> current = getFavourites(userID);
            if (current == null) current = new ArrayList<>();
            for (String symbol : favourites) {
                if (!current.contains(symbol)) current.add(symbol);
            }
            PreparedStatement statement = connection.prepareStatement(INSERT_UPDATE_FAVOURITES);
            statement.setInt(1, userID);
            statement.setString(2, String.join(",", current));
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    // Removes a single stock from the user's favourites list
    public void deleteFavourite(int userID, String favourites) {
        try {
            List<String> current = getFavourites(userID);
            if (current == null || current.isEmpty()) return;
            current.remove(favourites);
            PreparedStatement statement = connection.prepareStatement(INSERT_UPDATE_FAVOURITES);
            statement.setInt(1, userID);
            statement.setString(2, String.join(",", current));
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    // Returns the user's favourites as a list, empty list if they have none
    public List<String> getFavourites(int userID) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_FAVOURITES);
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            List<String> favourites = new ArrayList<>();
            while (resultSet.next()) {
                String favouritesString = resultSet.getString("favouritesList");
                if (favouritesString == null || favouritesString.isEmpty()) {
                    return favourites;
                }
                if (!favouritesString.isEmpty()) {
                    favourites.addAll(Arrays.asList(favouritesString.split(",")));
                }
            }
            return favourites;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    // Deletes the entire favourites row for a user - mainly used to reset state in tests
    public void clearFavourites(int userID) {
        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_USER_FAVOURITES);
            statement.setInt(1, userID);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}