package ni.edu.uam.facturacionapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.dao.ProductoDAO;
import ni.edu.uam.facturacionapp.modelo.Categoria;
import ni.edu.uam.facturacionapp.modelo.Producto;

import java.math.BigDecimal;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private TextField txtRutaImagen;
    @FXML private CheckBox chkActivo;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarCategorias();
        cargarProductos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getCategoria() != null ? cell.getValue().getCategoria().getNombre() : ""
        ));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
    }

    private void cargarCategorias() {
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaDAO.listar());
        cmbCategoria.setItems(categorias);
    }

    private void cargarProductos() {
        ObservableList<Producto> productos = FXCollections.observableArrayList(productoDAO.listar());
        tblProductos.setItems(productos);
    }

    @FXML
    private void onGuardar() {
        if (cmbCategoria.getValue() == null || txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Por favor llene los campos obligatorios y seleccione una categoría.");
            return;
        }

        try {
            Producto p = new Producto();
            p.setCodigo(txtCodigo.getText().trim());
            p.setNombre(txtNombre.getText().trim());
            p.setCategoria(cmbCategoria.getValue());
            p.setPrecioVenta(new BigDecimal(txtPrecio.getText().trim()));
            p.setExistencia(Integer.parseInt(txtExistencia.getText().trim()));
            p.setRutaImagen(txtRutaImagen.getText().trim());
            p.setActivo(chkActivo.isSelected());

            if (productoDAO.guardar(p)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado correctamente en PostgreSQL.");
                limpiarFormulario();
                cargarProductos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el producto.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Entrada", "Asegúrese de ingresar un precio válido y una cantidad numéricas.");
        }
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        txtRutaImagen.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}