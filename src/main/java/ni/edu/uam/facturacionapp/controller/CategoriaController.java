package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.modelo.Categoria;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    private void initialize() {
        configurarColumnas();
        cargarCategorias();
        chkActiva.setSelected(true);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));
    }

    private void cargarCategorias() {
        ObservableList<Categoria> lista = FXCollections.observableArrayList(categoriaDAO.listar());
        tblCategorias.setItems(lista);
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre de la categoría.");
            return;
        }

        Categoria categoria = new Categoria(null, nombre, chkActiva.isSelected());
        if (categoriaDAO.guardar(categoria)) {
            mensaje(Alert.AlertType.INFORMATION, "Categoría guardada correctamente en PostgreSQL.");
            limpiar();
            cargarCategorias();
        } else {
            mensaje(Alert.AlertType.ERROR, "Error al guardar la categoría.");
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void limpiar() {
        txtNombre.clear();
        chkActiva.setSelected(true);
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}