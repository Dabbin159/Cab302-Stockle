package com.stockle.test;

import org.junit.jupiter.api.Test;

import com.stockle.api.GroqService;

public class AI_API_Test {

    @Test
    public void testAiApi() {
        System.out.println("\n=== AI API Test Suite ===");
        unrelatedQuestionTest();
        askForStrategiesTest();
        rememberContextTest();
        System.out.println("\n=== All tests completed ===");
    }

    private static void unrelatedQuestionTest() {
        GroqService groqService = new GroqService();
        System.out.println("--- Test 1: Unrelated Question ---");
        String response = groqService.askChatbot("How can I bake a chocolate cake?");
        System.out.println("User: How can I bake a chocolate cake?");
        System.out.println("AI: " + response);
    }
    private static void askForStrategiesTest() {
        GroqService groqService = new GroqService();
        System.out.println("--- Test 2: Ask for Strategies ---");
        String response = groqService.askChatbot("Can you suggest some stock trading strategies for long-term investing?");
        System.out.println("User: Can you suggest some stock trading strategies for long-term investing?");
        System.out.println("AI: " + response);
    }
    private static void rememberContextTest() {
        GroqService groqService = new GroqService();
        System.out.println("--- Test 3: Remember Context ---");
        String response1 = groqService.askChatbot("I bought Tesla stock last week.");
        System.out.println("User: I bought Tesla stock last week.");
        System.out.println("AI: " + response1);
        String response2 = groqService.askChatbot("Was that a good decision?");
        System.out.println("User: Was that a good decision?");
        System.out.println("AI: " + response2);
    }

}

