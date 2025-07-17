module com.example.fileidenty {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.example.fileidenty to javafx.fxml;
    exports com.example.fileidenty;
}