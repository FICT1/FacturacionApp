package ni.edu.uam.facturacionapp.dao;

import ni.edu.uam.facturacionapp.util.DatabaseConnection;
import ni.edu.uam.facturacionapp.modelo.Categoria;
import ni.edu.uam.facturacionapp.modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public boolean guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia, p.ruta_imagen, p.activo,
                   c.id AS cat_id, c.nombre AS cat_nombre, c.activa AS cat_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            ORDER BY p.id ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria cat = new Categoria(
                        rs.getInt("cat_id"),
                        rs.getString("cat_nombre"),
                        rs.getBoolean("cat_activa")
                );

                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        cat,
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        rs.getString("ruta_imagen"),
                        rs.getBoolean("activo")
                );
                lista.add(p);
            }
        }
        return lista;
    }

    public boolean actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto
            SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        String sql = idExcluir == null
                ? "SELECT COUNT(*) FROM producto WHERE LOWER(codigo) = LOWER(?)"
                : "SELECT COUNT(*) FROM producto WHERE LOWER(codigo) = LOWER(?) AND id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo);
            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}