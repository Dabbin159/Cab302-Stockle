package com.stockle.database;

import java.sql.Connection;

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

    @Override
    public Boolean addFavourite(int userID, String favourites) {
        return false;
    }

    @Override
    public Boolean deleteFavourite(int userID, String favourites){
        return false;
    }

    @Override
    public String getFavourites(int userID){

        return null;
    }
}
