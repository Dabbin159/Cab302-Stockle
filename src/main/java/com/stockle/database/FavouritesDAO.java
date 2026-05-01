package com.stockle.database;

public interface FavouritesDAO {

    public Boolean addFavourite(int userID, String favourites);

    public Boolean deleteFavourite(int userID, String favourites);

    public String getFavourites(int userID); 
}
