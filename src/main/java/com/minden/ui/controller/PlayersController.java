package com.minden.ui.controller;

import com.minden.config.ServiceFactory;
import com.minden.dto.PlayerDto;
import com.minden.service.PlayerService;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class PlayersController {

    @FXML private TableView<PlayerDto> playersTable;
    @FXML private TableColumn<PlayerDto, Integer> idColumn;
    @FXML private TableColumn<PlayerDto, String> usernameColumn;
    @FXML private TableColumn<PlayerDto, String> emailColumn;
    @FXML private TableColumn<PlayerDto, Integer> goldColumn;
    @FXML private TableColumn<PlayerDto, Integer> energyColumn;

    @FXML private TextField searchField;
    
    // Фільтри
    @FXML private TextField minGoldFilter;
    @FXML private TextField maxGoldFilter;
    @FXML private TextField minEnergyFilter;
    @FXML private TextField maxEnergyFilter;

    private PlayerService playerService;
    private ObservableList<PlayerDto> playersData = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<PlayerDto> filteredData;

    @FXML
    public void initialize() {
        try {
            playerService = ServiceFactory.getInstance().getPlayerService();

            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            goldColumn.setCellValueFactory(new PropertyValueFactory<>("gold"));
            energyColumn.setCellValueFactory(new PropertyValueFactory<>("energy"));

            // Забираємо зайву порожню колонку справа
            playersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Налаштування фільтрації/пошуку
            filteredData = new FilteredList<>(playersData, p -> true);
            searchField.textProperty().addListener((observable, oldValue, newValue) -> updatePredicate());
            minGoldFilter.textProperty().addListener((observable, oldValue, newValue) -> updatePredicate());
            maxGoldFilter.textProperty().addListener((observable, oldValue, newValue) -> updatePredicate());
            minEnergyFilter.textProperty().addListener((observable, oldValue, newValue) -> updatePredicate());
            maxEnergyFilter.textProperty().addListener((observable, oldValue, newValue) -> updatePredicate());
            playersTable.setItems(filteredData);

            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        playersData.clear();
        playersData.addAll(playerService.findAll());
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    private void updatePredicate() {
        filteredData.setPredicate(player -> {
            // Фільтр за текстом (ім'я/email)
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                boolean matchesUsername = player.getUsername() != null && player.getUsername().toLowerCase().contains(lowerCaseFilter);
                boolean matchesEmail = player.getEmail() != null && player.getEmail().toLowerCase().contains(lowerCaseFilter);
                if (!matchesUsername && !matchesEmail) {
                    return false;
                }
            }

            // Фільтр по золоту
            try {
                if (minGoldFilter.getText() != null && !minGoldFilter.getText().trim().isEmpty()) {
                    int minGold = Integer.parseInt(minGoldFilter.getText().trim());
                    if (player.getGold() == null || player.getGold() < minGold) return false;
                }
                if (maxGoldFilter.getText() != null && !maxGoldFilter.getText().trim().isEmpty()) {
                    int maxGold = Integer.parseInt(maxGoldFilter.getText().trim());
                    if (player.getGold() == null || player.getGold() > maxGold) return false;
                }
            } catch (NumberFormatException ignored) {}

            // Фільтр по енергії
            try {
                if (minEnergyFilter.getText() != null && !minEnergyFilter.getText().trim().isEmpty()) {
                    int minEnergy = Integer.parseInt(minEnergyFilter.getText().trim());
                    if (player.getEnergy() == null || player.getEnergy() < minEnergy) return false;
                }
                if (maxEnergyFilter.getText() != null && !maxEnergyFilter.getText().trim().isEmpty()) {
                    int maxEnergy = Integer.parseInt(maxEnergyFilter.getText().trim());
                    if (player.getEnergy() == null || player.getEnergy() > maxEnergy) return false;
                }
            } catch (NumberFormatException ignored) {}

            return true;
        });
    }

    @FXML
    private void handleClearFilters(ActionEvent event) {
        searchField.clear();
        minGoldFilter.clear();
        maxGoldFilter.clear();
        minEnergyFilter.clear();
        maxEnergyFilter.clear();
    }


}
