# TV Maze API Middleware

API REST en Java (Spring Boot) que funciona como middleware de los servicios de [TV Maze](https://www.tvmaze.com/api).

## Requisitos

- Java 21
- Maven 3.9+

## Ejecutar

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Pruebas

```bash
mvn test
```

## Endpoints

### A. Búsqueda de shows

`GET /api/shows/search?q={search_query}`

```bash
curl "http://localhost:8080/api/shows/search?q=girls"
```

Respuesta:

```json
[
  {
    "id": 139,
    "name": "Girls",
    "channel": "HBO",
    "summary": "<p>This Emmy winning series is a comic look at...</p>",
    "genres": ["Drama", "Romance"]
  }
]
```

`channel` toma el nombre de `network` y, si el show no tiene cadena de TV, el de `webChannel`.

### Errores

Los errores se devuelven en formato [ProblemDetail (RFC 7807)](https://www.rfc-editor.org/rfc/rfc7807):

| Código | Caso |
|--------|------|
| 400 | Falta el parámetro `q` o está vacío |
| 502 | TV Maze no respondió o respondió con error |

## Estructura

```
controller/  Endpoints REST
service/     Lógica de negocio
client/      Consumo del API de TV Maze
mapper/      Conversión de respuestas de TV Maze a DTOs propios
dto/         Objetos de respuesta
exception/   Excepciones y manejo global de errores
config/      Configuración (RestClient, propiedades)
```
