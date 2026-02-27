module org.example.ostriv.ostriv {
  requires javafx.controls;
  requires javafx.fxml;

  opens org.example.ostriv.ostriv to javafx.fxml;
  opens org.example.ostriv.ostriv.map to javafx.fxml;
  exports org.example.ostriv.ostriv;
  exports org.example.ostriv.ostriv.map;
}