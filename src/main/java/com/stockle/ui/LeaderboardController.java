package com.stockle.ui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// INDEV FILE
public class LeaderboardController {

    @FXML private HBox podiumContainer;
    @FXML private VBox rankingsContainer;
    @FXML private javafx.scene.control.Button darkModeBtn;
    @FXML private javafx.scene.image.ImageView darkModeIcon;

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();

    // Init
    @FXML
    public void initialize() {
        syncThemeButton();
        List<MockData.LeaderboardEntry> entries = MockData.leaderboard();
        buildPodium(entries);
        buildRankings(entries);
    }

    @FXML
    private void toggleDarkMode() {
        if (podiumContainer == null || podiumContainer.getScene() == null) return;
        com.stockle.SessionManager sessionManager = com.stockle.SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(podiumContainer.getScene().getRoot());
        syncThemeButton();
    }

    private void syncThemeButton() {
        if (darkModeIcon == null) return;
        boolean dark = com.stockle.SessionManager.getInstance().isDarkModeEnabled();
        String iconPath = dark
            ? "/com/stockle/ui/images/light-mode-button.png"
            : "/com/stockle/ui/images/dark-mode-button.png";
        java.net.URL url = getClass().getResource(iconPath);
        if (url != null) darkModeIcon.setImage(new javafx.scene.image.Image(url.toExternalForm()));
    }

    // Podium

    private void buildPodium(List<MockData.LeaderboardEntry> entries) {
        if (entries.size() < 3) return;

        MockData.LeaderboardEntry first  = entries.get(0);
        MockData.LeaderboardEntry second = entries.get(1);
        MockData.LeaderboardEntry third  = entries.get(2);

        // Order: 2nd  1st  3rd
        podiumContainer.getChildren().addAll(
            podiumCard(second, "2nd", "podium-medal-silver", "podium-silver", 180),
            podiumCard(first,  "1st", "podium-medal-gold",   "podium-gold",   220),
            podiumCard(third,  "3rd", "podium-medal-bronze", "podium-bronze", 160)
        );
    }

    private VBox podiumCard(MockData.LeaderboardEntry e, String medal, String medalColorStyle, String medalStyle, double height) {
        Label medalLabel = label(medal, "podium-medal", medalColorStyle);

        Label name  = label(e.displayName(), "podium-name");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        Label value = label(CURRENCY.format(e.portfolioValue()), "podium-value");

        boolean pos = e.changePct() >= 0;
        String sign = pos ? "+" : "";
        Label change = label(String.format("%s%.1f%%", sign, e.changePct()),
            pos ? "podium-change-pos" : "podium-change-neg");

        VBox card = new VBox(8, medalLabel, name, value, change);
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(height);
        card.setMinHeight(height);
        card.getStyleClass().addAll("podium-card", medalStyle);
        card.setStyle("-fx-padding: 20 16;");
        return card;
    }

    // Rankings list

    private void buildRankings(List<MockData.LeaderboardEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            MockData.LeaderboardEntry e = entries.get(i);

            // Rank number
            boolean isTop3 = e.rank() <= 3;
            Label rankNum = label(String.valueOf(e.rank()), isTop3 ? "rank-number-top" : "rank-number");
            rankNum.setMinWidth(32);

            // Name + username
            Label name = label(e.displayName(), "rank-name");
            Label username = label(e.username(), "rank-username");
            VBox nameBox = new VBox(2, name, username);

            HBox left = new HBox(12, rankNum, nameBox);
            left.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(left, Priority.ALWAYS);

            // Value + change
            boolean pos = e.changePct() >= 0;
            String sign = pos ? "+" : "";
            Label value = label(CURRENCY.format(e.portfolioValue()), "rank-value");
            Label change = label(String.format("%s%.1f%%", sign, e.changePct()),
                pos ? "rank-change-pos" : "rank-change-neg");
            VBox valueBox = new VBox(2, value, change);
            valueBox.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(left, spacer, valueBox);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("rank-row");
            if (i < entries.size() - 1) row.getStyleClass().add("rank-row-divider");

            rankingsContainer.getChildren().add(row);
        }
    }

    // Helpers

    private Label label(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    // Navigation

    @FXML private void navDashboard() throws IOException { SceneManager.switchTo("dashboard/dashboard-view.fxml"); }
    @FXML private void navTrading()   throws IOException { SceneManager.switchTo("trading/trading-view.fxml"); }
    @FXML private void navAI()        throws IOException { SceneManager.switchTo("ai/ai-view.fxml"); }
    @FXML private void navNews()      throws IOException { SceneManager.switchTo("news/news-view.fxml"); }
    @FXML private void navProfile()   throws IOException { SceneManager.switchTo("profile/profile-view.fxml"); }

    @FXML
    private void handleSignOut() throws IOException {
        com.stockle.SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
