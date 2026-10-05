package ni.edu.uam.facturacionapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.dao.ProductoDAO;
import ni.edu.uam.facturacionapp.modelo.Categoria;
import ni.edu.uam.facturacionapp.modelo.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<Object> cmbFiltroCategoria;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    private ObservableList<Producto> productosMaster = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private Producto productoSeleccionado;
    private String rutaImagen;

    @FXML
    private void initialize() {
        configurarColumnas();
        cargarCategorias();
        cargarProductos();
        configurarFiltros();

        chkActivo.setSelected(true);

        cmbCategoria.setOnShowing(event -> cargarCategorias());

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                productoSeleccionado = newVal;
                cargarEnFormulario(productoSeleccionado);
            }
        });
    }

    private void configurarColumnas() {
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
        try {
            ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaDAO.listar());
            cmbCategoria.setItems(categorias);

            ObservableList<Object> opcionesFiltroCat = FXCollections.observableArrayList();
            opcionesFiltroCat.add("Todas");
            opcionesFiltroCat.addAll(categorias);
            cmbFiltroCategoria.setItems(opcionesFiltroCat);
            if (cmbFiltroCategoria.getSelectionModel().isEmpty()) {
                cmbFiltroCategoria.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible cargar las categorías.");
        }
    }

    private void cargarProductos() {
        try {
            productosMaster.setAll(productoDAO.listar());
            productosFiltrados = new FilteredList<>(productosMaster, p -> true);
            tblProductos.setItems(productosFiltrados);
            aplicarFiltro();
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible cargar la lista de productos.");
        }
    }

    private void configurarFiltros() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        cmbFiltroEstado.getSelectionModel().selectFirst();

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro());
        cmbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro());
        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro());
    }

    private void aplicarFiltro() {
        String textoBusqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String estadoFiltro = cmbFiltroEstado.getValue() == null ? "Todos" : cmbFiltroEstado.getValue();
        Object catFiltro = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(p -> {
            boolean coincideTexto = textoBusqueda.isEmpty()
                    || p.getCodigo().toLowerCase().contains(textoBusqueda)
                    || p.getNombre().toLowerCase().contains(textoBusqueda)
                    || (p.getCategoria() != null && p.getCategoria().getNombre().toLowerCase().contains(textoBusqueda));

            boolean coincideEstado = true;
            if ("Activos".equalsIgnoreCase(estadoFiltro)) {
                coincideEstado = p.isActivo();
            } else if ("Inactivos".equalsIgnoreCase(estadoFiltro)) {
                coincideEstado = !p.isActivo();
            }

            boolean coincideCategoria = true;
            if (catFiltro instanceof Categoria) {
                Categoria catSel = (Categoria) catFiltro;
                coincideCategoria = p.getCategoria() != null && p.getCategoria().getId().equals(catSel.getId());
            }

            return coincideTexto && coincideEstado && coincideCategoria;
        });
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    @FXML
    private void guardar() {
        try {
            Producto producto = obtenerProductoFormulario();

            if (productoDAO.existeCodigo(producto.getCodigo(), null)) {
                mostrarAdvertencia("Código duplicado", "Ya existe un producto registrado con ese código.");
                return;
            }

            if (productoDAO.guardar(producto)) {
                mostrarExito("Producto registrado", "La información fue almacenada correctamente.");
                limpiar();
                cargarProductos();
            }
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible registrar el producto.");
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            mostrarAdvertencia("Seleccione un producto", "Debe seleccionar el producto que desea actualizar.");
            return;
        }

        try {
            Producto productoForm = obtenerProductoFormulario();

            if (productoDAO.existeCodigo(productoForm.getCodigo(), productoSeleccionado.getId())) {
                mostrarAdvertencia("Código duplicado", "El código ingresado ya pertenece a otro producto.");
                return;
            }

            productoSeleccionado.setCodigo(productoForm.getCodigo());
            productoSeleccionado.setNombre(productoForm.getNombre());
            productoSeleccionado.setCategoria(productoForm.getCategoria());
            productoSeleccionado.setPrecioVenta(productoForm.getPrecioVenta());
            productoSeleccionado.setExistencia(productoForm.getExistencia());
            productoSeleccionado.setRutaImagen(rutaImagen);
            productoSeleccionado.setActivo(productoForm.isActivo());

            if (productoDAO.actualizar(productoSeleccionado)) {
                mostrarExito("Producto actualizado", "Los datos del producto fueron actualizados correctamente.");
                limpiar();
                cargarProductos();
            }
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible actualizar el producto.");
        }
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mostrarAdvertencia("Seleccione un producto", "Debe seleccionar el producto que desea eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de eliminar el producto '" + productoSeleccionado.getNombre() + "'?");

        Optional<ButtonType> respuesta = confirm.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                if (productoDAO.eliminar(productoSeleccionado.getId())) {
                    mostrarExito("Producto eliminado", "El producto fue eliminado correctamente.");
                    limpiar();
                    cargarProductos();
                }
            } catch (SQLException e) {
                mostrarError("Error de base de datos", "No fue posible eliminar el producto.");
            }
        }
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagen = null;
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private Producto obtenerProductoFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (codigo.isEmpty()) {
            txtCodigo.requestFocus();
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }

        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }

        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();
        if (categoria == null) {
            cmbCategoria.requestFocus();
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio debe ser un valor numérico.");
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }

        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia debe ser un número entero.");
        }

        if (existencia < 0) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }

        return new Producto(
                null,
                codigo,
                nombre,
                categoria,
                precio,
                existencia,
                rutaImagen,
                chkActivo.isSelected()
        );
    }

    private void cargarEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtPrecio.setText(p.getPrecioVenta() != null ? p.getPrecioVenta().toString() : "");
        txtExistencia.setText(String.valueOf(p.getExistencia()));
        chkActivo.setSelected(p.isActivo());
        rutaImagen = p.getRutaImagen();

        if (rutaImagen != null && !rutaImagen.isBlank()) {
            try {
                imgProducto.setImage(new Image(rutaImagen));
            } catch (Exception e) {
                imgProducto.setImage(null);
            }
        } else {
            imgProducto.setImage(null);
        }
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