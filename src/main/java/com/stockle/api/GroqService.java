package com.stockle.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.stockle.model.TradeContext;

public class GroqService {

    private static final String API_KEY = "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";
    // System.getenv("GROQ_API_KEY");

    private final HttpClient client = HttpClient.newHttpClient();

    public String askChatbot(String input) {
        String prompt = """
        You are a stock market learning assistant.

        Only answer stock-related questions.
        If unrelated, say you cannot help.

        User:
        """ + input;

        return sendRequest(prompt);
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

        return sendRequest(prompt);
    }

    private String sendRequest(String prompt) {
        try {
            String safePrompt = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String json = """
            {
            "model": "llama-3.1-8b-instant",
            "messages": [
                {
                "role": "user",
                "content": "%s"
                }
            ]
            }
            """.formatted(safePrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return extractMessage(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error contacting AI service.";
        }
    }

    private String extractMessage(String json) {
        try {
            int contentIndex = json.indexOf("\"content\":\"");
            if (contentIndex == -1) return "No response from AI.";

            int start = contentIndex + 11;

            StringBuilder result = new StringBuilder();
            boolean escape = false;

            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);

                if (escape) {
                    if (c == 'n') result.append('\n');
                    else result.append(c);
                    escape = false;
                } else {
                    if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        break;
                    } else {
                        result.append(c);
                    }
                }
            }

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing AI response.";
        }
    }
}


