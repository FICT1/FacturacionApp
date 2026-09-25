package ni.edu.uam.facturacionapp.modelo;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data


public class Categoria {
    private Integer id;
    private String nombre;
    private boolean activa;

    @Override
    public String toString() {
        return nombre;
    }
}
