package cl.ipss.saborgourmet.dto;

import cl.ipss.saborgourmet.model.Cliente;

/**
 * Datos de salida de un cliente.
 *
 * @param id       identificador único
 * @param rut      RUT del cliente
 * @param nombre   nombre completo
 * @param email    correo electrónico
 * @param telefono teléfono de contacto
 */
public record ClienteResponse(
        Long id,
        String rut,
        String nombre,
        String email,
        String telefono
) {
    /**
     * Convierte una entidad {@link Cliente} a su DTO de respuesta.
     *
     * @param cliente entidad origen
     * @return DTO de respuesta
     */
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getRut(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono()
        );
    }
}
