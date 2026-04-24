package com.stockle.test;

import com.stockle.api.GroqService;

public class AI_API_Test {
    public static void main(String[] args) {
        // AddUserTest();
        // int randomId = new Random().nextInt(SQLUserDAO.getInstance().getAllUsers().size()) + 1; // Get a random user ID from the database (Selects Random User)
        // GetUserByIdTest(randomId);
        // UpdateUserTestValidate(randomId);
        // UpdateUserTest(randomId);
        // UpdateUserTestValidate(randomId);
        // // UniqueUserTest();
    }

    private static void unrelatedQuestionTest() {
        GroqService groqService = new GroqService();
        String response = groqService.askChatbot("What is a stock?");
        System.out.println("Response: " + response);
    }
    private static void askForStrategiesTest() {
        GroqService groqService = new GroqService();
        String response = groqService.askChatbot("What is the weather today?");
        System.out.println("Response: " + response);
    }
    private static void rememberContextTest() {
        GroqService groqService = new GroqService();
        String response = groqService.askChatbot("What is the weather today?");
        System.out.println("Response: " + response);
    }
}

