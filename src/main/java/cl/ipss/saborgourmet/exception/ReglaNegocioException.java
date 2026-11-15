package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando se incumple una regla de negocio de entrada (HTTP 400).
 */
public class ReglaNegocioException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param mensaje detalle de la regla incumplida
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
