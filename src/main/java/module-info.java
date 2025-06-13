module org.example.project101game {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jetbrains.annotations;

    opens org.example.project101game.controllers to javafx.fxml;
    exports org.example.project101game;
    exports org.example.project101game.models;
}
