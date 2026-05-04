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

    private static final List<MockData.Article> ARTICLES = MockData.newsArticles();

    // Initialise
    @FXML
    public void initialize() {
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

        List<MockData.Article> filtered = ARTICLES.stream()
            .filter(a -> query.isEmpty()
                || a.headline().toLowerCase().contains(query)
                || a.summary().toLowerCase().contains(query)
                || a.symbols().stream().anyMatch(s -> s.toLowerCase().contains(query)))
            .filter(a -> allSyms || a.symbols().contains(sym))
            .collect(Collectors.toList());

        articleList.getChildren().clear();
        for (MockData.Article article : filtered) {
            articleList.getChildren().add(buildArticleCard(article));
        }
    }

    private VBox buildArticleCard(MockData.Article article) {
        // Source badge + date row
        Label sourceBadge = label(article.source(), "source-badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = label(article.date(), "article-date");
        HBox topRow = new HBox(6, sourceBadge, spacer, date);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label headline = label(article.headline(), "article-headline");
        Label summary  = label(article.summary(),  "article-summary");

        HBox tagsRow = new HBox(6);
        for (String s : article.symbols()) {
            tagsRow.getChildren().add(label(s, "symbol-tag"));
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
