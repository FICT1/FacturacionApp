package ni.edu.uam.facturacionapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/ni/edu/uam/facturacionapp/fxml/menu-principal.fxml"));
        stage.setTitle("Sistema de facturación");
        stage.setScene(new Scene(loader.load(), 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}