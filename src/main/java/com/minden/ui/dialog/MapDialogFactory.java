package com.minden.ui.dialog;

import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Factory for creating rich UI dialogs and overlay screens for player choices,
 * campfire/tavern events, and game victory conditions.
 */
public class MapDialogFactory {

    /**
     * Creates a StackPane containing the rest choice dialog overlay.
     *
     * @param isInCity true if the player is currently inside a city tile
     * @param playerGold the current amount of gold the player owns
     * @param onChooseRest callback receiving true for safe rest (tavern) and
     * false for dangerous rest (camp)
     * @param onCancel callback invoked when the user cancels the action
     * @return the constructed StackPane overlay
     */
    public static StackPane createRestChoiceOverlay(boolean isInCity, int playerGold,
            Consumer<Boolean> onChooseRest, Runnable onCancel) {
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(26, 18, 12, 0.85); -fx-alignment: center;");

        VBox dialogBox = new VBox(20);
        dialogBox.setStyle("-fx-background-color: #faf6ec; "
                + "-fx-border-color: #a67c52; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px; "
                + "-fx-padding: 30px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(27,20,14,0.4), 15, 0, 0, 8);");
        dialogBox.setMaxSize(480, 350);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("⛺ Вибір місця для ночівлі");
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #8c3b2b;");

        Label descLabel = new Label(isInCity
                ? "Ви перебуваєте в безпечних стінах міста. Де ви бажаєте заночувати?"
                : "Навколо лише дика природа та небезпеки. Ви можете розбити табір тут безкоштовно, але це ризиковано.");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-text-fill: #3d2612; -fx-text-alignment: center;");

        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);

        Button wildRestBtn = new Button(isInCity ? "Спати на вулиці міста (Безкоштовно, небезпечно)" : "Розбити табір (Безкоштовно, небезпечно)");
        wildRestBtn.setStyle("-fx-background-color: #c66347; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;");
        wildRestBtn.setOnAction(e -> onChooseRest.accept(false));

        wildRestBtn.setOnMouseEntered(e -> wildRestBtn.setStyle("-fx-background-color: #d9785c; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));
        wildRestBtn.setOnMouseExited(e -> wildRestBtn.setStyle("-fx-background-color: #c66347; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));

        buttonsBox.getChildren().add(wildRestBtn);

        if (isInCity) {
            int tavernCost = 15;
            Button tavernRestBtn = new Button("Орендувати кімнату в Таверні (💰 " + tavernCost + " Золота, безпечно)");
            tavernRestBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;");

            tavernRestBtn.setOnAction(e -> {
                if (playerGold >= tavernCost) {
                    onChooseRest.accept(true);
                } else {
                    descLabel.setText("❌ У вас недостатньо золота для кімнати в таверні!");
                    descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b84f3d; -fx-text-alignment: center;");
                }
            });

            tavernRestBtn.setOnMouseEntered(e -> tavernRestBtn.setStyle("-fx-background-color: #57825e; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));
            tavernRestBtn.setOnMouseExited(e -> tavernRestBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));

            buttonsBox.getChildren().add(tavernRestBtn);
        }

        Button cancelBtn = new Button("Назад");
        cancelBtn.setStyle("-fx-background-color: #e8dfcd; -fx-text-fill: #3d2612; -fx-font-family: 'Georgia'; -fx-font-size: 13px; -fx-padding: 8px 16px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> onCancel.run());

        dialogBox.getChildren().addAll(titleLabel, descLabel, buttonsBox, cancelBtn);
        backdrop.getChildren().add(dialogBox);
        return backdrop;
    }

    /**
     * Creates a StackPane containing the event outcome dialog overlay.
     *
     * @param title the event title
     * @param description the detailed event description
     * @param goldLost the amount of gold lost during the event
     * @param energyRestored the amount of energy restored
     * @param onContinue callback invoked when the user confirms and continues
     * @return the constructed StackPane overlay
     */
    public static StackPane createEventOverlay(String title, String description, int goldLost,
            int energyRestored, Runnable onContinue) {
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(26, 18, 12, 0.85); -fx-alignment: center;");

        VBox dialogBox = new VBox(20);
        dialogBox.setStyle("-fx-background-color: #faf6ec; -fx-border-color: #a67c52; -fx-border-width: 2px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 30px; -fx-effect: dropshadow(three-pass-box, rgba(27,20,14,0.4), 15, 0, 0, 8);");
        dialogBox.setMaxSize(450, 320);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("🔥 Подія: " + title);
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #8c3b2b;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-text-fill: #3d2612; -fx-alignment: center; -fx-text-alignment: center;");

        HBox statsBox = new HBox(25);
        statsBox.setAlignment(Pos.CENTER);

        Label energyDiff = new Label("⚡ +" + energyRestored + " Енергії");
        energyDiff.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b779a;");

        Label goldDiff = new Label(goldLost > 0 ? "💰 -" + goldLost + " Золота" : "💰 Без втрат");
        goldDiff.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (goldLost > 0 ? "#b84f3d;" : "#4b7252;"));

        statsBox.getChildren().addAll(energyDiff, goldDiff);

        Button closeButton = new Button("Продовжити подорож");
        closeButton.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        closeButton.setOnAction(e -> onContinue.run());

        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #57825e; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));

        dialogBox.getChildren().addAll(titleLabel, descLabel, statsBox, closeButton);
        backdrop.getChildren().add(dialogBox);
        return backdrop;
    }

    /**
     * Creates a StackPane containing the victory achievement dialog overlay.
     *
     * @param username the username of the player
     * @param days the total number of days spent
     * @param gold the total amount of gold collected
     * @param onExit callback invoked when the user exits to the menu
     * @return the constructed StackPane overlay
     */
    public static StackPane createVictoryOverlay(String username, int days, int gold, Runnable onExit) {
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(26, 18, 12, 0.9); -fx-alignment: center;");

        VBox victoryBox = new VBox(25);
        victoryBox.setStyle("-fx-background-color: #faf6ec; "
                + "-fx-border-color: #c5a059; "
                + "-fx-border-width: 3px; "
                + "-fx-border-radius: 16px; "
                + "-fx-background-radius: 16px; "
                + "-fx-padding: 40px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(140,94,56,0.3), 20, 0, 0, 0);");
        victoryBox.setMaxSize(500, 380);
        victoryBox.setAlignment(Pos.CENTER);

        Label trophyLabel = new Label("🏆");
        trophyLabel.setStyle("-fx-font-size: 64px;");

        Label titleLabel = new Label("ВЕЛИКА ПЕРЕМОГА!");
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #8c3b2b;");

        String victoryText = String.format(
                "Вітаємо, %s!\nВи знайшли всі приховані скарби на острові!\n"
                + "Ви витратили: %d днів (ходів).\n"
                + "Ваше фінальне золото: 💰 %d",
                username, days, gold
        );

        Label descLabel = new Label(victoryText);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16px; -fx-text-fill: #3d2612; -fx-text-alignment: center; -fx-line-spacing: 5px;");

        Button exitBtn = new Button("Повернутися в меню");
        exitBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12px 25px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> onExit.run());

        exitBtn.setOnMouseEntered(ev -> exitBtn.setStyle("-fx-background-color: #57825e; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12px 25px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));
        exitBtn.setOnMouseExited(ev -> exitBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12px 25px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));

        victoryBox.getChildren().addAll(trophyLabel, titleLabel, descLabel, exitBtn);
        backdrop.getChildren().add(victoryBox);
        return backdrop;
    }
}
