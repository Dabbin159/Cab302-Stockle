package com.stockle.api.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * ApiClient encapsulates HTTP client configuration and retry logic.
 * @param API_KEY Alpaca API key
 * @param SECRET_KEY Alpaca secret key
 * @param BASE_URL alpaca base trading url
 * @param DATA_URL alpaca market data url
 * @param CONNECT_TIMEOUT connection timeout duration
 * @param REQUEST_TIMEOUT request timeout duration
 * @param MAX_RETRIES maximum number of retry attempts for failed requests
 * @param RETRY_DELAY_MS base delay in milliseconds for retry backoff
 * @param httpClient the HttpClient instance used for making requests
 * 
 */
public class ApiClient {
    public static final String API_KEY = "PK4A3ZKIHWXHTZT54S7O7UZNRF"; // had hardcoded keys here for easier testing between group members
    public static final String SECRET_KEY = "6x4GW4U5ozqS46zvyk1pvay1h4dXswEPakEvZEtBW7Rh"; // These keys no longer work
    public static final String BASE_URL = "https://paper-api.alpaca.markets";
    public static final String DATA_URL = "https://data.alpaca.markets";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final HttpClient httpClient;

    /**
     * Constructs a new ApiClient with a configured HttpClient.
     */
    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Executes an HTTP GET request to the specified URL with automatic retry logic.
     * 
     * @param url the URL endpoint to request
     * @return the response body as a String
     * @throws Exception if the request fails after all retry attempts are exhausted or if a non-retryable error occurs
     */
    public String makeRequest(String url) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .header("APCA-API-KEY-ID", API_KEY)
                        .header("APCA-API-SECRET-KEY", SECRET_KEY)
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    if (response.statusCode() >= 500 && attempt < MAX_RETRIES) {
                        long delayMs = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                        Thread.sleep(delayMs);
                        continue;
                    }
                    throw new RuntimeException("API Error: " + response.statusCode() + " - " + response.body());
                }

                return response.body();
            } catch (InterruptedException | java.net.http.HttpTimeoutException e) {
                if (attempt == MAX_RETRIES) {
                    throw new RuntimeException("Request failed after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
                long delayMs = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                Thread.sleep(delayMs);
            }
        }
        throw new RuntimeException("Request failed after " + MAX_RETRIES + " attempts");
    }
}
