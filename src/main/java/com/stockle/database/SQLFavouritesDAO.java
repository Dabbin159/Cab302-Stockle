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
    
    private SQLFavouritesDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

    public static SQLFavouritesDAO getInstance() {
        if (instance == null) {
            instance = new SQLFavouritesDAO();
        }
        return instance;
    }

    private static final String ADD_FAVOURITE = "INSERT INTO favourites (userID, favouritesList) VALUES (?, ?) ON CONFLICT(userID) DO UPDATE SET favouritesList = favouritesList || ',' || excluded.favouritesList";
    private static final String DELETE_FAVOURITE = "DELETE FROM favourites where userID = ?";
    private static final String GET_FAVOURITES = "SELECT favouritesList FROM favourites where userID = ?";

    @Override
    public void addFavourite(int userID, List<String> favourites) {
        try {
            PreparedStatement statement = connection.prepareStatement(ADD_FAVOURITE);
            statement.setInt(1, userID);
            statement.setString(2, String.join(",", favourites));
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteFavourite(int userID, String favourites){
        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_FAVOURITE);
            statement.setInt(1, userID);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<String> getFavourites(int userID){
        try {
            PreparedStatement statement = connection.prepareStatement(GET_FAVOURITES);
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            List<String> favourites = new ArrayList<>();
            while (resultSet.next()) {
                String favouritesString = resultSet.getString("favouritesList");
                if (favouritesString.isEmpty()) {
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
        return null;
    }
}