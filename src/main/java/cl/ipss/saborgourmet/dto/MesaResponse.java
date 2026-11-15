package cl.ipss.saborgourmet.dto;

import cl.ipss.saborgourmet.model.Mesa;

/**
 * Datos de salida de una mesa.
 *
 * @param id        identificador único
 * @param numero    número visible de la mesa
 * @param capacidad capacidad máxima de personas
 * @param ubicacion sector del restaurante
 * @param activa    indica si la mesa está habilitada
 */
public record MesaResponse(
        Long id,
        Integer numero,
        Integer capacidad,
        String ubicacion,
        boolean activa
) {
    /**
     * Convierte una entidad {@link Mesa} a su DTO de respuesta.
     *
     * @param mesa entidad origen
     * @return DTO de respuesta
     */
    public static MesaResponse from(Mesa mesa) {
        return new MesaResponse(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getUbicacion(),
                mesa.isActiva()
        );
    }
}
