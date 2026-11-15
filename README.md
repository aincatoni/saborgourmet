# EVALUACIÓN SUMATIVA UNIDAD Nº 2
## DESARROLLO DE SOFTWARE WEB II

---

### DATOS DE IDENTIFICACIÓN
* **Institución:** Instituto Profesional San Sebastián (IPSS)
* **Carrera:** Ingeniería en Conectividad y Redes / Informática
* **Asignatura:** Desarrollo de Software Web II
* **Unidad Temática:** Unidad 2 – API REST con Spring Boot, Arquitectura CSR y Persistencia en JSON
* **Docente:** Victor Israel Cofre Farias
* **Estudiante(s):** Ain Catoni - Pía Alarcón
* **Sección:** 51
* **Año:** 2026

---

# Sabor Gourmet API

API REST desarrollada con Spring Boot (Java 21) para la gestión de clientes, mesas, disponibilidad y reservas del restaurante **Sabor Gourmet**, utilizando arquitectura **CSR (Controller → Service → Repository)** y persistencia en archivo JSON mediante Jackson.

## Requisitos

- Java 21 (OpenJDK 21)
- Maven Wrapper incluido (`./mvnw`)

## Ejecución

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Persistencia en JSON

- **Ruta configurada** (`src/main/resources/application.properties`):
  ```properties
  app.datos.ruta=./data/datos.json
  ```
- Si `data/datos.json` no existe al iniciar, la aplicación crea la carpeta y genera un archivo válido vacío:
  ```json
  { "clientes": [], "mesas": [], "reservas": [] }
  ```
- Todas las operaciones de escritura se realizan de forma atómica a través de `JsonDataStore` (`datos.json.tmp` → `Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)`).

### Estructura de `data/datos.json`

```json
{
  "clientes": [
    { "id": 1, "rut": "12345678-9", "nombre": "Ana Pérez", "email": "ana@mail.com", "telefono": "+56911112222" }
  ],
  "mesas": [
    { "id": 1, "numero": 1, "capacidad": 2, "ubicacion": "Salón", "activa": true }
  ],
  "reservas": [
    {
      "id": 1,
      "clienteId": 1,
      "mesaId": 2,
      "fecha": "2026-11-20",
      "turno": "CENA_1",
      "cantidadPersonas": 3,
      "estado": "ACTIVA",
      "observaciones": ""
    }
  ]
}
```

## Endpoints principales

- **Clientes:** `GET /api/clientes`, `GET /api/clientes/{id}`, `POST /api/clientes`, `PUT /api/clientes/{id}`, `DELETE /api/clientes/{id}`
- **Mesas:** `GET /api/mesas`, `GET /api/mesas/{id}`, `GET /api/mesas/disponibles?fecha=YYYY-MM-DD&turno=CENA_1&personas=4`, `POST /api/mesas`, `PUT /api/mesas/{id}`, `DELETE /api/mesas/{id}`
- **Reservas:** `GET /api/reservas?fecha=&estado=`, `GET /api/reservas/{id}`, `POST /api/reservas`, `PUT /api/reservas/{id}`, `PATCH /api/reservas/{id}/cancelar`

## Pruebas en Postman

Importar la colección ubicada en `postman/SaborGourmet.postman_collection.json` en Postman y ejecutar las peticiones en orden con la variable `baseUrl = http://localhost:8080`.

## Evidencias de pruebas

Las capturas de ejecución en Postman y prueba de persistencia tras reinicio se encuentran en [`capturas/`](capturas/).

### Clientes
| Caso | Evidencia |
|---|---|
| Listar clientes (`GET /api/clientes`) | ![Listar clientes](capturas/listar_clientes.png) |
| Obtener cliente por ID (`GET /api/clientes/{id}`) | ![Obtener cliente](capturas/listar_cliente.png) |
| Crear cliente (`POST /api/clientes`) | ![Crear cliente](capturas/crear_cliente.png) |
| Crear cliente (caso adicional) | ![Crear cliente 01](capturas/crear_cliente_01.png) |
| Actualizar cliente (`PUT /api/clientes/{id}`) | ![Actualizar cliente](capturas/actualizar_cliente.png) |
| Eliminar cliente con reserva activa (`409`) | ![Eliminar cliente con reserva activa](capturas/eliminar_cliente_con_reserva_activa.png) |
| Eliminar cliente sin reservas activas (`204`) | ![Eliminar cliente sin reserva activa](capturas/eliminar_cliente_sin_reserva_activa.png) |

### Mesas y disponibilidad
| Caso | Evidencia |
|---|---|
| Listar mesas (`GET /api/mesas`) | ![Listar mesas](capturas/mesas_listar.png) |
| Crear mesa (`POST /api/mesas`) | ![Crear mesa](capturas/mesas_crear.png) |
| Consultar disponibilidad (`GET /api/mesas/disponibles`) | ![Consultar disponibilidad](capturas/mesas_consultar_disponibilidad.png) |
| Eliminar mesa con reserva activa (`409`) | ![Eliminar mesa con reserva activa](capturas/mesas_eliminar_mesa_con_reserva.png) |
| Eliminar mesa sin reservas activas (`204`) | ![Eliminar mesa sin reserva activa](capturas/mesas_eliminar_sin_reserva.png) |

### Reservas
| Caso | Evidencia |
|---|---|
| Listar reservas (`GET /api/reservas`) | ![Listar reservas](capturas/reservas_listar.png) |
| Crear reserva válida (`201`) | ![Crear reserva válida](capturas/reservas_crear_reserva_valida.png) |
| Crear reserva superpuesta (`409`) | ![Crear reserva superpuesta](capturas/reserveras_crear_reserva_superpuesta.png) |
| Crear reserva excede capacidad (`400`) | ![Crear reserva excede capacidad](capturas/reserva_crear_reserva_excede_cantidad.png) |
| Crear reserva fecha pasada / datos inválidos (`400`) | ![Crear reserva fecha pasada](capturas/reservas_crear_resrrva_fecha_pasada.png) |
| Crear reserva cliente inexistente (`404`) | ![Crear reserva cliente inexistente](capturas/reservas_crear_reserva_cliente_inexistente.png) |
| Modificar reserva / cambio de turno (`200`) | ![Modificar reserva](capturas/reservas_modificar_reserva_cambio_turno.png) |
| Cancelar reserva (`PATCH /api/reservas/{id}/cancelar`, `200`) | ![Cancelar reserva](capturas/reservas_cancelar.png) |
| Cancelar reserva nuevamente (`409`) | ![Cancelar reserva nuevamente](capturas/reservas_cancelar_nuevamente.png) |
| Obtener reserva inexistente (`404`) | ![Obtener reserva inexistente](capturas/reservas_obtener_reserva_inexistente.png) |

### Prueba de reinicio (persistencia en JSON)
| Paso | Evidencia |
|---|---|
| Reinicio 01 | ![Reinicio 01](capturas/reinicio/reinicio_01.png) |
| Reinicio 02 | ![Reinicio 02](capturas/reinicio/reinicio_02.png) |
| Reinicio 03 | ![Reinicio 03](capturas/reinicio/reinicio_03.png) |
| Reinicio 04 | ![Reinicio 04](capturas/reinicio/reinicio_04.png) |
