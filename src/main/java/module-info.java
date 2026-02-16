module ca.thepopbox {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    
    opens ca.thepopbox to javafx.fxml;
    exports ca.thepopbox;
}
