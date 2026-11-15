package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.exception.PersistenciaException;
import cl.ipss.saborgourmet.model.BaseDatos;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Única clase responsable de leer y escribir el archivo JSON de persistencia.
 * Garantiza inicialización segura, concurrencia sincronizada y escritura atómica
 * mediante archivo temporal y reemplazo atómico.
 */
@Component
public class JsonDataStore {

    private final Path rutaArchivo;
    private final ObjectMapper objectMapper;
    private BaseDatos datos;

    /**
     * Construye el almacén de datos JSON con la ruta configurada y el mapper de Jackson.
     *
     * @param ruta         ruta al archivo JSON (propiedad {@code app.datos.ruta})
     * @param objectMapper instancia de Jackson administrada por Spring
     */
    public JsonDataStore(@Value("${app.datos.ruta:./data/datos.json}") String ruta,
                         ObjectMapper objectMapper) {
        this.rutaArchivo = Paths.get(ruta);
        this.objectMapper = objectMapper;
    }

    /**
     * Inicializa el almacén al arrancar la aplicación.
     * Si el archivo no existe, crea las carpetas padre y escribe una estructura vacía válida.
     * Si existe, lo carga en memoria; si está corrupto, lanza {@link PersistenciaException}
     * sin sobrescribir el archivo original.
     */
    @PostConstruct
    public synchronized void inicializar() {
        try {
            if (Files.notExists(rutaArchivo)) {
                Path padre = rutaArchivo.getParent();
                if (padre != null) {
                    Files.createDirectories(padre);
                }
                this.datos = new BaseDatos();
                guardar();
            } else {
                this.datos = objectMapper.readValue(rutaArchivo.toFile(), BaseDatos.class);
                if (this.datos == null) {
                    throw new PersistenciaException("El archivo de datos está vacío o es inválido", null);
                }
            }
        } catch (PersistenciaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("No se pudo cargar el archivo de datos: " + rutaArchivo, e);
        }
    }

    /**
     * Retorna la instancia en memoria de la base de datos.
     *
     * @return contenedor {@link BaseDatos}
     */
    public synchronized BaseDatos getDatos() {
        return datos;
    }

    /**
     * Persiste el estado actual en disco de forma atómica.
     * Escribe primero en un archivo temporal {@code .tmp} con formato legible y luego
     * reemplaza el archivo destino con {@link StandardCopyOption#ATOMIC_MOVE}.
     */
    public synchronized void guardar() {
        Path padre = rutaArchivo.getParent();
        Path temporal = rutaArchivo.resolveSibling(rutaArchivo.getFileName() + ".tmp");
        try {
            if (padre != null) {
                Files.createDirectories(padre);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporal.toFile(), datos);
            Files.move(temporal, rutaArchivo,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException ignored) {
                // Se ignora si no se pudo limpiar el temporal
            }
            throw new PersistenciaException("Error al guardar los datos en " + rutaArchivo, e);
        }
    }
}
