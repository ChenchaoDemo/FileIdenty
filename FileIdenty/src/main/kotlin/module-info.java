module com.example.fileidenty {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.apache.commons.compress;  // 加上这行
    requires tess4j;
    opens com.example.fileidenty to javafx.fxml;
    exports com.example.fileidenty;
}