package cl.ipss.saborgourmet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada para crear o actualizar un cliente.
 *
 * @param rut      RUT del cliente (obligatorio, único)
 * @param nombre   nombre completo (obligatorio)
 * @param email    correo electrónico válido (obligatorio, único)
 * @param telefono teléfono de contacto (obligatorio)
 */
public record ClienteRequest(
        @NotBlank(message = "El RUT es obligatorio")
        String rut,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono
) {
}
