package ni.edu.uam.facturacionapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private void abrirProductos() {
        abrirVentana("/ni/edu/uam/facturacionapp/fxml/producto-view.fxml", "Gestión de Productos");
    }

    @FXML
    private void abrirCategorias() {
        abrirVentana("/ni/edu/uam/facturacionapp/fxml/categoria-view.fxml", "Gestión de Categorías");
    }

    @FXML
    private void salir() {
        Platform.exit();
    }

    private void abrirVentana(String rutaFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}