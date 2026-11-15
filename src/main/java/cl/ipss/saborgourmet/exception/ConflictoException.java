package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando ocurre un conflicto de estado o unicidad (HTTP 409).
 */
public class ConflictoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param mensaje detalle del conflicto
     */
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
