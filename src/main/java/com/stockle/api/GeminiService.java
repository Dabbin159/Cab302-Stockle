package com.stockle.api;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import com.stockle.model.TradeContext;

// API KEY IS "AIzaSyCsqgMFKyuROC19krctupJ7wdduo0SSNqQ"

public class GeminiService {
    private Client client;

    public GeminiService() {
        this.client = new Client();
    }


    public String askChatbot(String input) {
        String prompt = """
You are a stock market learning assistant.

Only answer stock-related questions.
If unrelated, say you cannot help.

User:
""" + input;
        GenerateContentResponse response = client.models.generateContent(
            "gemini-3-flash-preview",
            prompt,
            null
        );

        return response.text();
    }
    public String getMultiPerspectiveAdvice(TradeContext ctx) {
        String prompt = """
You are a trading coach.

Trade:
- Action: %s
- Price Change: %.2f%%
- Profit/Loss: $%.2f
- Holding Days: %d

Give perspectives:
Momentum Trader:
Long-Term Investor:
Risk Manager:
Contrarian Trader:

Rules:
- 1-2 sentences each
- No predictions
- No saying right/wrong
""".formatted(
                ctx.action,
                ctx.priceChangePercent,
                ctx.profitLoss,
                ctx.holdingDays
            );
            GenerateContentResponse response = client.models.generateContent(
                "gemini-2-flash",
                prompt,
                null
            );
            return response.text();
    }
}


