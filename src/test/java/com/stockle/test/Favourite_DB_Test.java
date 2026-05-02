package com.stockle.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.stockle.database.SQLFavouritesDAO;

public class Favourite_DB_Test {

    private SQLFavouritesDAO favouritesDAO;
        
    @Test
    public void testGetFavouritesMany() {
        List<String> favouritesString = Arrays.asList("AAPL", "TSLA", "BOBS");

        assertEquals(favouritesString,1);
    }

    @Test 
    void TestGetFavouritesOne() {
        List<String> favouritesString = Arrays.asList("AAPL");

        assertEquals(favouritesString, 1);
    }

    @Test 
    void TestGetFavouritesNone() {
        List<String> favouritesString = new ArrayList<>();

        assertEquals(favouritesString, 0);
    }
}
