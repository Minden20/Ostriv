package com.minden.ui.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minden.config.ServiceFactory;
import com.minden.entity.ActionLog;
import com.minden.entity.Event;
import com.minden.entity.PlayerEventHistory;
import com.minden.repository.ActionLogRepository;
import com.minden.repository.EventRepository;
import com.minden.repository.PlayerRepository;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class LogsController {

    @FXML
    private TableView<ActionLog> logsTable;
    @FXML
    private TableColumn<ActionLog, String> createdAtColumn;
    @FXML
    private TableColumn<ActionLog, String> adventureColumn;

    private ActionLogRepository actionLogRepository;
    private PlayerRepository playerRepository;
    private EventRepository eventRepository;

    private ObservableList<ActionLog> logsData = FXCollections.observableArrayList();
    private Map<Integer, Event> eventsMap = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            var serviceFactory = ServiceFactory.getInstance();
            actionLogRepository = serviceFactory.getActionLogRepository();
            playerRepository = serviceFactory.getPlayerRepository();
            eventRepository = serviceFactory.getEventRepository();

            for (Event event : eventRepository.findAll()) {
                eventsMap.put(event.getId(), event);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            createdAtColumn.setCellValueFactory(cellData -> {
                ActionLog log = cellData.getValue();
                LocalDateTime ldt = log.getCreatedAt();
                String timeStr = ldt != null ? ldt.format(formatter) : "";

                if ("EVENT".equals(log.getActionType()) && log.getFromX() != null) {
                    if (!timeStr.isEmpty()) {
                        return new SimpleStringProperty(String.format("День %d (%s)", log.getFromX(), timeStr));
                    }
                    return new SimpleStringProperty(String.format("День %d", log.getFromX()));
                }
                return new SimpleStringProperty(timeStr);
            });

            adventureColumn.setCellValueFactory(cellData -> {
                return new SimpleStringProperty(formatAdventure(cellData.getValue()));
            });

            logsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося завантажити щоденник: " + e.getMessage());
        }
    }

    private String formatAdventure(ActionLog log) {
        if (log == null) {
            return "";
        }

        String actionType = log.getActionType() != null ? log.getActionType().toUpperCase() : "";
        int fx = log.getFromX() != null ? log.getFromX() : 0;
        int fy = log.getFromY() != null ? log.getFromY() : 0;
        int tx = log.getToX() != null ? log.getToX() : 0;
        int ty = log.getToY() != null ? log.getToY() : 0;
        boolean valid = log.getIsValid() != null ? log.getIsValid() : true;

        switch (actionType) {
            case "MOVE":
                if (valid) {
                    return String.format("Ви вирушили в дорогу з координат (%d, %d) та успішно перейшли на (%d, %d).", fx, fy, tx, ty);
                } else {
                    return String.format("Ви намагалися пройти з координат (%d, %d) до (%d, %d), але шлях виявився заблокованим або небезпечним.", fx, fy, tx, ty);
                }
            case "COLLECT":
            case "TREASURE":
                return String.format("На координатах (%d, %d) ви знайшли захований скарб! Ваші кишені поважчали від золота.", fx, fy);
            case "EVENT":
                Event event = eventsMap.get(fy);
                if (event != null) {
                    return String.format("На вашому шляху трапилася подія '%s': %s", event.getName(), event.getDescription());
                }
                return "Несподівана подія трапилася з вами на дорозі. Дорога завжди повна пригод!";
            default:
                return String.format("Ви здійснили дію %s на координатах (%d, %d).", actionType, fx, fy);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        try {
            logsData.clear();
            var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
            Integer playerId = currentUser != null ? currentUser.getId() : null;

            if (playerId != null) {
                List<ActionLog> dbLogs = actionLogRepository.findByPlayerId(playerId);
                logsData.addAll(dbLogs);

                List<PlayerEventHistory> eventHistory = playerRepository.getHistoryByPlayerId(playerId);
                int currentDay = currentUser.getCurrentDay() != null ? currentUser.getCurrentDay() : 1;

                for (PlayerEventHistory history : eventHistory) {
                    int diffDays = currentDay - (history.getOccurredDay() != null ? history.getOccurredDay() : 1);
                    LocalDateTime eventTime = LocalDateTime.now().minusDays(Math.max(0, diffDays));

                    ActionLog virtualEventLog = ActionLog.builder()
                            .id(-history.getId())
                            .playerId(playerId)
                            .actionType("EVENT")
                            .fromX(history.getOccurredDay())
                            .fromY(history.getEventId())
                            .isValid(true)
                            .createdAt(eventTime)
                            .build();

                    logsData.add(virtualEventLog);
                }

                logsData.sort((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                        return 0;
                    }
                    if (a.getCreatedAt() == null) {
                        return 1;
                    }
                    if (b.getCreatedAt() == null) {
                        return -1;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                });
            }

            logsTable.setItems(logsData);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити щоденник: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
