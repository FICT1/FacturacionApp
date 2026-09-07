package ni.edu.uam.facturacionapp.modelo;

import lombok.*;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Empleado {
    private Integer id;
    private String nombres;
    private String apellidos;
    private Cargo cargo;
    private LocalDate fechaContratacion;
    private boolean activo;

}
