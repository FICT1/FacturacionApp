package ni.edu.uam.facturacionapp.modelo;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Cargo {
    private Integer id;
    private String nombre;
    private String descripcion;
}
