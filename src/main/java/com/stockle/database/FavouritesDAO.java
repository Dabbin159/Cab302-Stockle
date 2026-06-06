package com.stockle.database;

import java.util.List;

public interface FavouritesDAO {

    /**
     * Adds a list of favourite stock symbols for a user.
     * @param userID the ID of the user
     * @param favourites the list of stock symbols to add to the user's favourites
     */
    public void addFavourite(int userID, List<String> favourites);

    /**
     * Deletes a list of favourite stock symbols for a user.
     * @param userID the ID of the user
     * @param favourites the list of stock symbols to remove from the user's favourites
     */
    public void deleteFavourite(int userID, String favourites);
    
    /**
     * Retrieves the list of favourite stock symbols for a user.
     * @param userID the ID of the user
     * @return the list of favourite stock symbols for the user
     */
    public List<String> getFavourites(int userID); 

    /**
     * Clears all favourite stock symbols for a user.
     * @param userID the ID of the user whose favourites are to be cleared
     */
    public void clearFavourites(int userID);
}
