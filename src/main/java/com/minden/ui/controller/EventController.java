package com.minden.ui.controller;

import com.minden.config.ServiceFactory;
import com.minden.dto.EventDto;
import com.minden.service.EventService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class EventController {
    @FXML
    private TableView<EventDto> eventsTable;
    @FXML
    private TableColumn<EventDto, Integer> idColumn;
    @FXML
    private TableColumn<EventDto, String> nameColumn;
    @FXML
    private TableColumn<EventDto, String> descriptionColumn;
    @FXML
    private TableColumn<EventDto, Integer> minGoldPenaltyColumn;
    @FXML
    private TableColumn<EventDto, Integer> maxGoldPenaltyColumn;

    private EventService eventService;
    private ObservableList<EventDto> eventsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            eventService = ServiceFactory.getInstance().getEventService();
            idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            descriptionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
            minGoldPenaltyColumn
                    .setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("minGoldPenalty"));
            maxGoldPenaltyColumn
                    .setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maxGoldPenalty"));
            eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        eventsData.clear();
        eventsData.addAll(eventService.findAll());
        eventsTable.setItems(eventsData);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }
}