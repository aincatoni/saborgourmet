package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe (HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param mensaje detalle del recurso no encontrado
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
