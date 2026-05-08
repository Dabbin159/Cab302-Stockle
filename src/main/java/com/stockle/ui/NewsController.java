package com.stockle.ui;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.NewsArticle;
import com.stockle.api.service.NewsService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class NewsController {
    @FXML private TextField searchField;
    @FXML private VBox articleList;
    @FXML private ScrollPane mainScroll;
    @FXML private javafx.scene.image.ImageView darkModeIcon;

    private final NewsService newsService = new NewsService(
        new ApiClient(),
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    );

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    private String nextPageToken = null;
    private String currentSymbol = null;
    private boolean isLoading = false;

    private final Label loadingLabel = styledLabel("Loading…", "article-loading");
    private final Label emptyLabel   = styledLabel("No articles found.", "article-empty");

    // Initialise
    @FXML
    public void initialize() {
        syncThemeButton();
        
        // Search on Enter; clear search restores global feed
        searchField.setOnAction(e -> {
            String text = searchField.getText().trim().toUpperCase();
            resetAndLoad(text.isEmpty() ? null : text);
        });
        searchField.textProperty().addListener((obs, o, n) -> {
            if (n.isEmpty()) resetAndLoad(null);
        });

        // Infinite scroll — load next page when within 15 % of the bottom
        mainScroll.vvalueProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 0.85 && !isLoading && nextPageToken != null) {
                loadNextPage();
            }
        });

        resetAndLoad(null);
    }

    //  Loading
    private void resetAndLoad(String symbol) {
        currentSymbol = symbol;
        nextPageToken = null;
        articleList.getChildren().clear();
        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoading) return;
        isLoading = true;

        articleList.getChildren().remove(emptyLabel);
        if (!articleList.getChildren().contains(loadingLabel)) {
            articleList.getChildren().add(loadingLabel);
        }

        final String sym = currentSymbol;
        final String token = nextPageToken;

        Thread.ofVirtual().start(() -> {
            NewsService.NewsPage page = newsService.getNewsPage(20, token, sym);

            Platform.runLater(() -> {
                articleList.getChildren().remove(loadingLabel);

                if (page.articles().isEmpty() && articleList.getChildren().isEmpty()) {
                    articleList.getChildren().add(emptyLabel);
                } else {
                    for (NewsArticle article : page.articles()) {
                        articleList.getChildren().add(buildArticleCard(article));
                    }
                }

                nextPageToken = page.nextPageToken();
                isLoading = false;
            });
        });
    }

    // Card builder 
    private VBox buildArticleCard(NewsArticle article) {
        // Source badge + date row
        String badgeText = (article.author != null && !article.author.isBlank()) ? article.author : (article.source != null ? article.source : "News");
        Label sourceBadge = styledLabel(badgeText, "source-badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = styledLabel(formatDate(article.published_at), "article-date");
        HBox topRow = new HBox(6, sourceBadge, spacer, date);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label headline = styledLabel(article.headline != null ? article.headline : "", "article-headline");
        headline.setWrapText(true);

        HBox tagsRow = new HBox(6);
        if (article.symbols != null) {
            for (String sym : article.symbols) {
                tagsRow.getChildren().add(styledLabel(sym, "symbol-tag"));
            }
        }

        VBox card = new VBox(10, topRow, headline, tagsRow);

        if (article.summary != null && !article.summary.isBlank()) {
            Label summary = styledLabel(article.summary, "article-summary");
            summary.setWrapText(true);
            card.getChildren().add(2, summary);
        }

        if (article.url != null && !article.url.isBlank()) {
            Label urlLabel = styledLabel(article.url, "article-url");
            urlLabel.setWrapText(false);
            urlLabel.setEllipsisString("...");
            urlLabel.setMaxWidth(Double.MAX_VALUE);
            urlLabel.setOnMouseClicked(e -> {
                javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(article.url);
                cb.setContent(content);
                urlLabel.setText("Copied!");
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> urlLabel.setText(article.url));
                }).start();
            });
            card.getChildren().add(urlLabel);
        }

        card.getStyleClass().add("article-card");
        return card;
    }

    // Helpers 
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            return DATE_FMT.format(Instant.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }

    private Label styledLabel(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    @FXML
    private void toggleDarkMode() {
        if (searchField == null || searchField.getScene() == null) {
            return;
        }

        com.stockle.SessionManager sessionManager = com.stockle.SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(searchField.getScene().getRoot());
        syncThemeButton();
    }

    private void syncThemeButton() {
        if (darkModeIcon == null) {
            return;
        }

        boolean darkModeEnabled = com.stockle.SessionManager.getInstance().isDarkModeEnabled();
        String iconPath = darkModeEnabled
            ? "/com/stockle/ui/images/light-mode-button.png"
            : "/com/stockle/ui/images/dark-mode-button.png";

        java.net.URL iconUrl = getClass().getResource(iconPath);
        if (iconUrl != null) {
            darkModeIcon.setImage(new javafx.scene.image.Image(iconUrl.toExternalForm()));
        }
    }

    // Navigation
    @FXML private void navDashboard()   throws IOException { SceneManager.switchTo("dashboard/dashboard-view.fxml"); }
    @FXML private void navTrading()     throws IOException { SceneManager.switchTo("trading/trading-view.fxml"); }
    @FXML private void navAI()          throws IOException { SceneManager.switchTo("ai/ai-view.fxml"); }
    @FXML private void navLeaderboard() throws IOException { SceneManager.switchTo("leaderboard/leaderboard-view.fxml"); }

    @FXML
    private void handleSignOut() throws IOException {
        com.stockle.SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
