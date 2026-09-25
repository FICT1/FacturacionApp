module ni.edu.uam.facturacionapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql; // <-- Línea requerida para JDBC y PostgreSQL

    opens ni.edu.uam.facturacionapp to javafx.graphics;
    opens ni.edu.uam.facturacionapp.controller to javafx.fxml;
    opens ni.edu.uam.facturacionapp.modelo to javafx.base;

    exports ni.edu.uam.facturacionapp;
}