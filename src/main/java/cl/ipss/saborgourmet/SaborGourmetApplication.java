package cl.ipss.saborgourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de Sabor Gourmet.
 */
@SpringBootApplication
public class SaborGourmetApplication {

    /**
     * Inicia la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(SaborGourmetApplication.class, args);
    }
}
