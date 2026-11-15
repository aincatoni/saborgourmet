package cl.ipss.saborgourmet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Datos de entrada para crear o actualizar una mesa.
 *
 * @param numero    número visible de la mesa (obligatorio, positivo, único)
 * @param capacidad capacidad máxima de personas (obligatorio, positivo)
 * @param ubicacion sector del restaurante (opcional)
 * @param activa    indica si la mesa está habilitada (por defecto {@code true} si es {@code null})
 */
public record MesaRequest(
        @NotNull(message = "El número de mesa es obligatorio")
        @Positive(message = "El número de mesa debe ser positivo")
        Integer numero,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser positiva")
        Integer capacidad,

        String ubicacion,

        Boolean activa
) {
}
