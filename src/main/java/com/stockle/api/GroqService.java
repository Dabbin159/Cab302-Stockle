package com.stockle.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.stockle.model.TradeContext;

/**
 * Small service for talking to the Groq chat API.
 *
 * Keeps the prompts in one place and turns the API response into plain text
 * that the rest of the app can use.
 */
public class GroqService {

    private static final String API_KEY = "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";
    // System.getenv("GROQ_API_KEY");

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Sends a stock-related question to the chatbot.
     *
     * Only stock questions are meant to get through here. Anything unrelated is
     * pushed back so the reply stays on topic.
     *
     * @param input the user's message
     * @return the chatbot reply as plain text
     */
    public String askChatbot(String input) {
        String prompt = """
        You are a stock market learning assistant.

        Only answer stock-related questions.
        If unrelated, say you cannot help.

        User:
        """ + input;

        return sendRequest(prompt);
    }

    /**
     * Asks the AI for a few different views on a trade.
     *
     * The reply is framed from different trading styles so the user can see the
     * same trade through a few lenses.
     *
     * @param ctx the trade context used to build the prompt
     * @return the AI response as plain text
     */
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

    /**
     * Builds the request, sends it to Groq, and returns the text reply.
     *
     * @param prompt the prompt to send to the model
     * @return the extracted assistant message, or an error message if something fails
     */
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
                    /**
                     * Pulls the assistant message out of the raw JSON response.
                     *
                     * @param json the response body returned by Groq
                     * @return the message text, or a fallback message if it cannot be read
                     */

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


