package com.stockle.database;

import java.util.List;

public interface FavouritesDAO {

    public void addFavourite(int userID, List<String> favourites);

    public void deleteFavourite(int userID, String favourites);

    public List<String> getFavourites(int userID); 
}
