package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando falla la lectura o escritura del archivo JSON (HTTP 500).
 */
public class PersistenciaException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje y la causa original.
     *
     * @param mensaje detalle del error de persistencia
     * @param causa   excepción subyacente
     */
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
