package com.stockle.ui;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class NewsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> symbolFilter;
    @FXML private VBox articleList;

    // Mock data 
    record Article(String headline, String summary, String source,
                   String date, List<String> symbols) {}

    private static final List<Article> ARTICLES = List.of(
        new Article(
            "Apple Leader in Phone Sales in China for Second Straight Month With 23.6% Share",
            "Apple maintained its top position in China's smartphone market for the second consecutive month, capturing a 23.6% market share according to the latest research data.",
            "Benzinga", "Dec 31, 2024", List.of("AAPL")
        ),
        new Article(
            "Tesla Reports Record Q4 Deliveries, Beating Analyst Expectations",
            "Tesla delivered a record number of vehicles in Q4, surpassing Wall Street estimates and sending shares higher in pre-market trading.",
            "Reuters", "Jan 2, 2025", List.of("TSLA")
        ),
        new Article(
            "Federal Reserve Signals Potential Rate Cuts in 2025 as Inflation Cools",
            "Fed officials indicated they may cut interest rates multiple times in 2025, citing cooling inflation and a stable labour market in the latest FOMC meeting minutes.",
            "Bloomberg", "Jan 3, 2025", List.of("SPY", "QQQ")
        ),
        new Article(
            "Microsoft Azure Revenue Surges 33% on AI Demand, Shares Hit All-Time High",
            "Microsoft's cloud division posted stronger-than-expected growth driven by enterprise AI adoption, pushing the stock to a new record close.",
            "CNBC", "Jan 4, 2025", List.of("MSFT")
        ),
        new Article(
            "Nvidia Announces Next-Gen Blackwell Ultra GPU Architecture at CES 2025",
            "Nvidia unveiled its next generation of AI accelerator chips at CES, reinforcing its dominance in the data centre GPU market ahead of earnings season.",
            "MarketWatch", "Jan 7, 2025", List.of("NVDA")
        ),
        new Article(
            "Amazon Web Services Wins $10B Pentagon Cloud Contract",
            "Amazon secured a major multi-year cloud computing deal with the US Department of Defense, a significant win over competing bids from Microsoft and Google.",
            "Reuters", "Jan 8, 2025", List.of("AMZN")
        ),
        new Article(
            "S&P 500 Closes at Record High as Tech Rally Continues Into New Year",
            "The S&P 500 reached a fresh all-time high on the back of strong gains in the technology sector, with investors optimistic about earnings season.",
            "Bloomberg", "Jan 9, 2025", List.of("SPY", "AAPL", "MSFT", "NVDA")
        ),
        new Article(
            "Google Parent Alphabet Faces EU Antitrust Fine Over Search Advertising",
            "The European Commission is preparing a significant fine against Alphabet for alleged anti-competitive behaviour in online search advertising markets.",
            "CNBC", "Jan 10, 2025", List.of("GOOGL")
        ),
        new Article(
            "Meta Plans $40B AI Infrastructure Investment Across 2025",
            "Meta Platforms announced it will significantly increase capital expenditure on AI data centres and computing infrastructure throughout the coming year.",
            "MarketWatch", "Jan 11, 2025", List.of("META")
        ),
        new Article(
            "JPMorgan Posts Record Annual Profit Driven by Investment Banking Rebound",
            "JPMorgan Chase reported its highest-ever annual profit, boosted by a surge in investment banking fees and strong consumer spending activity.",
            "Benzinga", "Jan 13, 2025", List.of("JPM")
        )
    );

    // Initialise
    @FXML
    public void initialize() {
        // Populate symbol filter
        symbolFilter.getItems().add("All Symbols");
        ARTICLES.stream()
            .flatMap(a -> a.symbols().stream())
            .distinct().sorted()
            .forEach(s -> symbolFilter.getItems().add(s));
        symbolFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, o, n) -> renderArticles());
        symbolFilter.setOnAction(e -> renderArticles());

        renderArticles();
    }

    // Rendering

    private void renderArticles() {
        String query = searchField.getText().toLowerCase().trim();
        String sym   = symbolFilter.getValue();
        boolean allSyms = sym == null || sym.equals("All Symbols");

        List<Article> filtered = ARTICLES.stream()
            .filter(a -> query.isEmpty()
                || a.headline().toLowerCase().contains(query)
                || a.summary().toLowerCase().contains(query)
                || a.symbols().stream().anyMatch(s -> s.toLowerCase().contains(query)))
            .filter(a -> allSyms || a.symbols().contains(sym))
            .collect(Collectors.toList());

        articleList.getChildren().clear();
        for (Article article : filtered) {
            articleList.getChildren().add(buildArticleCard(article));
        }
    }

    private VBox buildArticleCard(Article article) {
        // Source badge + date row
        Label sourceBadge = label(article.source(), "source-badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = label(article.date(), "article-date");
        HBox topRow = new HBox(6, sourceBadge, spacer, date);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Headline
        Label headline = label(article.headline(), "article-headline");

        // Summary
        Label summary = label(article.summary(), "article-summary");

        // Symbol tags
        HBox tagsRow = new HBox(6);
        for (String sym : article.symbols()) {
            tagsRow.getChildren().add(label(sym, "symbol-tag"));
        }

        VBox card = new VBox(10, topRow, headline, summary, tagsRow);
        card.getStyleClass().add("article-card");
        return card;
    }

    private Label label(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    // Navigation 
    @FXML private void navDashboard() throws IOException { SceneManager.switchTo("dashboard/dashboard-view.fxml"); }
    @FXML private void navTrading() throws IOException { SceneManager.switchTo("trading/trading-view.fxml"); }
    @FXML private void navAI() throws IOException { SceneManager.switchTo("ai/ai-view.fxml"); }

    @FXML
    private void handleSignOut() throws IOException {
        com.stockle.SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
