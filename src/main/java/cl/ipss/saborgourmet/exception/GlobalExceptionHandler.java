package cl.ipss.saborgourmet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones que traduce errores a respuestas {@link ProblemDetail}
 * sin exponer trazas internas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de DTOs anotados con {@code @Valid} (HTTP 400).
     *
     * @param ex excepción de validación
     * @return respuesta 400 con detalle de campos inválidos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Error de validación en los datos enviados");
        problem.setTitle("Datos inválidos");
        problem.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Maneja errores de validación en parámetros de métodos de controlador (HTTP 400).
     *
     * @param ex excepción de validación de método
     * @return respuesta 400
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> manejarValidacionMetodo(HandlerMethodValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Parámetros de solicitud inválidos");
        problem.setTitle("Datos inválidos");
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Maneja incumplimientos de reglas de negocio de entrada (HTTP 400).
     *
     * @param ex excepción de regla de negocio
     * @return respuesta 400
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ProblemDetail> manejarReglaNegocio(ReglaNegocioException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Solicitud inválida");
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Maneja cuerpos JSON malformados, tipos incompatibles o parámetros faltantes (HTTP 400).
     *
     * @param ex excepción de lectura o tipo
     * @return respuesta 400
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ProblemDetail> manejarSolicitudMalformada(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Formato de solicitud o parámetro inválido");
        problem.setTitle("Solicitud malformada");
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Maneja recursos inexistentes (HTTP 404).
     *
     * @param ex excepción de recurso no encontrado
     * @return respuesta 404
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso no encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Maneja conflictos de estado, unicidad o superposición (HTTP 409).
     *
     * @param ex excepción de conflicto
     * @return respuesta 409
     */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ProblemDetail> manejarConflicto(ConflictoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflicto");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Maneja errores de persistencia o excepciones inesperadas sin exponer trazas (HTTP 500).
     *
     * @param ex excepción capturada
     * @return respuesta 500 genérica
     */
    @ExceptionHandler({PersistenciaException.class, Exception.class})
    public ResponseEntity<ProblemDetail> manejarErrorInterno(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor");
        problem.setTitle("Error interno");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
