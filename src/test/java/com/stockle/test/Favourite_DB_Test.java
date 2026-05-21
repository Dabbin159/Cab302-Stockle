package com.stockle.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.stockle.database.SQLFavouritesDAO;

public class Favourite_DB_Test {

    private SQLFavouritesDAO favouritesDAO;

    @BeforeEach
    public void setUp() {
        favouritesDAO = SQLFavouritesDAO.getInstance();
        // Clear favourites for userID 1 before each test
        favouritesDAO.clearFavourites(1);
    }
        
    @Test
    public void testGetFavouritesMany() {
        List<String> favouritesString = Arrays.asList("AAPL", "TSLA", "BOBS");
        favouritesDAO.addFavourite(1, favouritesString);
        List<String> favourites = favouritesDAO.getFavourites(1);
        assertEquals(favouritesString,favourites);
    }

    @Test 
    void TestGetFavouritesOne() {
        List<String> favouritesString = Arrays.asList("AAPL");
        favouritesDAO.addFavourite(1, favouritesString);
        List<String> favourites = favouritesDAO.getFavourites(1);
        assertEquals(favouritesString, favourites);
    }

    @Test 
    void TestGetFavouritesNone() {
        List<String> favouritesString = new ArrayList<>();
        favouritesDAO.addFavourite(1, favouritesString);
        List<String> favourites = favouritesDAO.getFavourites(1);
        assertEquals(favouritesString, favourites);
    }

    @Test
    void TestAddingMultipleFavouries() {
        List<String> favouritesString = Arrays.asList("AAPL", "TSLA", "BOBS", "GOOG");
        List<String> favouritesSplit1 = Arrays.asList("AAPL", "TSLA");
        List<String> favouritesSplit2 = Arrays.asList("BOBS", "GOOG");
        favouritesDAO.addFavourite(1, favouritesSplit1);
        favouritesDAO.addFavourite(1, favouritesSplit2);
        List<String> favourites = favouritesDAO.getFavourites(1);
        assertEquals(favouritesString, favourites);
    }
}
