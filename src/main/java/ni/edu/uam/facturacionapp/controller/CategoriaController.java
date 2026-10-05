package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.modelo.Categoria;

import java.sql.SQLException;
import java.util.Optional;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Categoria categoriaSeleccionada;

    @FXML
    private void initialize() {
        configurarColumnas();
        cargarCategorias();
        chkActiva.setSelected(true);

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                categoriaSeleccionada = newVal;
                txtNombre.setText(categoriaSeleccionada.getNombre());
                chkActiva.setSelected(categoriaSeleccionada.isActiva());
            }
        });
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));
    }

    private void cargarCategorias() {
        try {
            ObservableList<Categoria> lista = FXCollections.observableArrayList(categoriaDAO.listar());
            tblCategorias.setItems(lista);
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible cargar las categorías.");
        }
    }

    @FXML
    private void guardar() {
        try {
            Categoria categoria = obtenerCategoriaFormulario();

            if (categoriaDAO.existeNombre(categoria.getNombre(), null)) {
                mostrarAdvertencia("Registro duplicado", "Ya existe una categoría con ese nombre.");
                return;
            }

            if (categoriaDAO.guardar(categoria)) {
                mostrarExito("Categoría registrada", "La categoría se guardó correctamente.");
                limpiar();
                cargarCategorias();
            }
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible registrar la categoría.");
        }
    }

    @FXML
    private void actualizar() {
        if (categoriaSeleccionada == null) {
            mostrarAdvertencia("Seleccione una categoría", "Debe seleccionar la categoría que desea actualizar.");
            return;
        }

        try {
            Categoria categoriaForm = obtenerCategoriaFormulario();

            if (categoriaDAO.existeNombre(categoriaForm.getNombre(), categoriaSeleccionada.getId())) {
                mostrarAdvertencia("Registro duplicado", "Ya existe otra categoría con ese nombre.");
                return;
            }

            categoriaSeleccionada.setNombre(categoriaForm.getNombre());
            categoriaSeleccionada.setActiva(categoriaForm.isActiva());

            if (categoriaDAO.actualizar(categoriaSeleccionada)) {
                mostrarExito("Categoría actualizada", "Los datos fueron modificados correctamente.");
                limpiar();
                cargarCategorias();
            }
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible actualizar la categoría.");
        }
    }

    @FXML
    private void eliminar() {
        if (categoriaSeleccionada == null) {
            mostrarAdvertencia("Seleccione una categoría", "Debe seleccionar la categoría que desea eliminar.");
            return;
        }

        try {
            if (categoriaDAO.tieneProductos(categoriaSeleccionada.getId())) {
                mostrarAdvertencia("Categoría con productos", "No puede eliminar la categoría porque tiene productos asociados.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Está seguro de eliminar la categoría '" + categoriaSeleccionada.getNombre() + "'?");

            Optional<ButtonType> resp = confirm.showAndWait();
            if (resp.isPresent() && resp.get() == ButtonType.OK) {
                if (categoriaDAO.eliminar(categoriaSeleccionada.getId())) {
                    mostrarExito("Categoría eliminada", "La categoría fue eliminada correctamente.");
                    limpiar();
                    cargarCategorias();
                }
            }
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible eliminar la categoría.");
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActiva.setSelected(true);
        categoriaSeleccionada = null;
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private Categoria obtenerCategoriaFormulario() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        return new Categoria(null, nombre, chkActiva.isSelected());
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}