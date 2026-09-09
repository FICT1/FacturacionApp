module ni.edu.uam.facturacionapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens ni.edu.uam.facturacionapp to javafx.graphics;
    opens ni.edu.uam.facturacionapp.controller to javafx.fxml;
    opens ni.edu.uam.facturacionapp.modelo to javafx.base;

    exports ni.edu.uam.facturacionapp;
}