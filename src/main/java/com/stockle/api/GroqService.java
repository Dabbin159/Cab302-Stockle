package com.stockle.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.stockle.model.Holding;
import com.stockle.model.Trade;
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
     * Formats portfolio data into a readable, human-friendly string used as
     * context for AI prompts.
     *
     * The returned text includes a "Current Holdings" section followed by
     * a "Recent Trades" list (most recent 10 trades). Each holding shows the
     * company id, quantity and average price. Each trade entry shows the
     * action (BUY/SELL), quantity, company name, per-share price and
     * timestamp.
     *
     * @param trades   array of recent trades (may be null or empty)
     * @param holdings list of current holdings (may be null or empty)
     * @return formatted portfolio context to include in AI prompts
     */
    public String formatPortfolioForAI(Trade[] trades, List<Holding> holdings) {
        StringBuilder portfolio = new StringBuilder("User Portfolio Context:\n\n");
        
        // Current holdings
        portfolio.append("Current Holdings:\n");
        if (holdings != null && !holdings.isEmpty()) {
            for (Holding h : holdings) {
                double avgPrice = (double) h.getAveragePrice();
                portfolio.append(String.format("- %s: %d shares @ $%.2f avg\n", 
                    h.getCompanyID(), h.getQuantity(), avgPrice));
            }
        } else {
            portfolio.append("- No current holdings\n");
        }
        
        // Recent trades (last 10)
        portfolio.append("\nRecent Trades (last 10):\n");
        if (trades != null && trades.length > 0) {
            int start = Math.max(0, trades.length - 10);
            for (int i = start; i < trades.length; i++) {
                Trade t = trades[i];
                String action = t.isType() ? "SELL" : "BUY";
                double perShare = t.getQuantity() != 0 ? (double) t.getTotalValue() / t.getQuantity() : 0.0;
                portfolio.append(String.format("- %s: %d shares of %s @ $%.2f | %s\n",
                    action, t.getQuantity(), t.getStock().getCompanyName(),
                    perShare, t.getTimeStamp()));
            }
        } else {
            portfolio.append("- No trades yet\n");
        }
        return portfolio.toString();
    }

    /**
     * Builds a coaching prompt using the provided portfolio context and
     * user question, then asks the AI for practical coaching advice.
     *
     * This method wraps the portfolio and question into a structured prompt
     * that requests pattern identification, risk observations, and
     * actionable suggestions.
     *
     * @param portfolioData previously formatted portfolio context
     * @param userQuestion  the user's follow-up question to guide the coach
     * @return the assistant reply as plain text
     */
    public String analyzeTradesAndCoach(String portfolioData, String userQuestion) {
        String prompt = """
        You are an experienced trading coach and mentor.
        
        %s
        
        User Question: %s
        
        Provide coaching advice based on their portfolio and trading history:
        1. Identify patterns in their buying/selling
        2. Note risk management observations
        3. Suggest improvements
        4. Give actionable tips
        
        Keep it practical and supportive—no predictions of future prices.
        """.formatted(portfolioData, userQuestion);
        
        return sendRequest(prompt);
    }

    /**
     * Produces a short textual summary of recent trading activity and asks
     * the AI to interpret patterns.
     *
     * The method computes basic aggregates (counts and totals) and builds
     * a prompt that asks the model to comment on timing, concentration and
     * risk management.
     *
     * @param trades array of trades to analyze
     * @return the AI's analysis as plain text
     */
    public String analyzeTradingPatterns(Trade[] trades) {
        StringBuilder analysisData = new StringBuilder();
        
        int buyCount = 0, sellCount = 0;
        long totalBuyValue = 0, totalSellValue = 0;
        
        for (Trade t : trades) {
            if (t.isType()) { // sell
                sellCount++;
                totalSellValue += t.getTotalValue();
            } else { // buy
                buyCount++;
                totalBuyValue += t.getTotalValue();
            }
        }
        
        analysisData.append(String.format("""
            Trading Summary:
            - Total Trades: %d
            - Buys: %d (Total: $%.2f)
            - Sells: %d (Total: $%.2f)
            - Buy/Sell Ratio: %.2f
            """, 
            trades.length, buyCount, (double) totalBuyValue, 
            sellCount, (double) totalSellValue,
            sellCount > 0 ? (double) buyCount / sellCount : (buyCount > 0 ? Double.POSITIVE_INFINITY : 0)));
        
        String prompt = """
        You are a trading pattern analyst.
        
        %s
        
        Based on this trading history, what patterns do you observe?
        - Are they buying before selling? (Good timing?)
        - Any concentration in certain stocks?
        - Risk management observations
        - Suggestions for improvement
        
        Be constructive and educational.
        """.formatted(analysisData.toString());
        
        return sendRequest(prompt);
    }

    /**
     * Sends a user message to the AI together with the user's portfolio
     * context so the assistant can provide answers grounded in the user's
     * actual holdings and trade history.
     *
     * @param userMessage   the user's question or prompt
     * @param portfolioData formatted portfolio context (from
     *                      {@link #formatPortfolioForAI})
     * @return the assistant reply as plain text
     */
    public String chatWithPortfolioContext(String userMessage, String portfolioData) {
        String prompt = """
        You are a helpful stock market coach and mentor.
        
        %s
        
        User's Question: %s
        
        Answer their question thoughtfully, considering their portfolio and trading history.
        Keep answers practical and educational. Avoid predictions.
        """.formatted(portfolioData, userMessage);
        
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

    /**
     * Pulls the assistant "content" field out of the raw JSON response
     * returned by the Groq chat endpoint.
     *
     * This is a small, tolerant extractor that unescapes simple sequences
     * and returns the assistant message text. If the expected fields are
     * missing or parsing fails, a short error message is returned.
     *
     * @param json raw response body from the chat API
     * @return extracted assistant message, or a short error string
     */
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


