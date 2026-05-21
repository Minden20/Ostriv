package com.minden.ui.controller;

import com.minden.config.ServiceFactory;
import com.minden.dto.TreasureDto;
import com.minden.service.TreasureService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TreasureController {
    @FXML
    private TableView<TreasureDto> treasuresTable;
    @FXML
    private TableColumn<TreasureDto, Integer> idColumn;
    @FXML
    private TableColumn<TreasureDto, Integer> xColumn;
    @FXML
    private TableColumn<TreasureDto, Integer> yColumn;
    @FXML
    private TableColumn<TreasureDto, Integer> minGoldColumn;
    @FXML
    private TableColumn<TreasureDto, Integer> maxGoldColumn;
    @FXML
    private TableColumn<TreasureDto, Boolean> isCollectedColumn;

    private TreasureService treasureService;
    private ObservableList<TreasureDto> treasuresData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            treasureService = ServiceFactory.getInstance().getTreasureService();
            idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            xColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("x"));
            yColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("y"));
            minGoldColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("minGold"));
            maxGoldColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maxGold"));
            isCollectedColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("isCollected"));

            treasuresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        treasuresData.clear();
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        Integer playerId = currentUser != null ? currentUser.getId() : null;
        treasuresData.addAll(treasureService.findAllForPlayer(playerId));
        treasuresTable.setItems(treasuresData);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }
}
