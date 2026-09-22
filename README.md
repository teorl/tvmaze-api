# TV Maze API Middleware

API REST en Java (Spring Boot) que funciona como middleware de los servicios de [TV Maze](https://www.tvmaze.com/api).

- **Búsqueda de shows** con una versión reducida de cada show y sus comentarios.
- **Detalle de un show** con todos los campos de TV Maze, en caché en MongoDB, y sus comentarios.
- **Comentarios y calificaciones** por show, guardados en MongoDB.

Tecnologías: Java 21, Spring Boot 3.5 (Web, Validation, Data MongoDB), `RestClient`, springdoc-openapi,
JUnit 5, Mockito y MockMvc.

## Inicio rápido

Requiere Java 21, Maven y una cadena de conexión de MongoDB (por ejemplo, de Atlas).

**1. Definir `MONGODB_URI`**

```bash
# macOS / Linux / Git Bash
export MONGODB_URI="mongodb+srv://usuario:password@cluster/tvmaze"
```

```powershell
# Windows (PowerShell)
$env:MONGODB_URI = "mongodb+srv://usuario:password@cluster/tvmaze"
```

**2. Ejecutar la aplicación**

```bash
mvn spring-boot:run
```

**3. Abrir Swagger UI** en [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html) y probar
los endpoints desde el navegador (*Try it out*). Por ejemplo, buscar `girls`, abrir el show `139` y comentarlo.

Los detalles de cada paso están en las secciones siguientes.

## Requisitos

- Java 21
- Maven 3.9+
- Un cluster de [MongoDB Atlas](https://www.mongodb.com/atlas) (o cualquier instancia de MongoDB)

## Configuración de MongoDB

La aplicación lee la cadena de conexión de la variable de entorno `MONGODB_URI` y usa la base de datos `tvmaze`.
Las credenciales nunca se escriben en el código ni en archivos versionados (`.env` está en `.gitignore`).

En [`.env.example`](.env.example) hay una plantilla del formato:

```
MONGODB_URI=mongodb+srv://usuario:password@cluster/tvmaze
```

Spring Boot no lee archivos `.env` por sí solo, así que la variable debe existir en la terminal antes de ejecutar
la aplicación. Si se ejecuta desde un IDE, hay que definirla en la configuración de ejecución.

**Windows (PowerShell)**

```powershell
# Solo para la sesión actual de la terminal
$env:MONGODB_URI = "mongodb+srv://usuario:password@cluster/tvmaze"

# O cargarla desde un archivo .env (copia de .env.example)
Get-Content .env | Where-Object { $_ -match '^[^#].*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    Set-Item "env:$name" $value
}
```

**macOS / Linux**

```bash
# Solo para la sesión actual de la terminal
export MONGODB_URI="mongodb+srv://usuario:password@cluster/tvmaze"

# O cargarla desde un archivo .env (copia de .env.example)
set -a; source .env; set +a
```

> En Atlas, agrega tu IP en *Network Access* y usa un usuario de base de datos con permisos de lectura y escritura.

**Si falla la resolución de `mongodb+srv://`**

En algunas redes (por ejemplo, con DNS IPv6) la consulta del registro SRV falla con errores como
`querySrv ECONNREFUSED` o `Unable to look up SRV record for host ...`. En ese caso usa la cadena
estándar, que lista los nodos del cluster explícitamente:

```
MONGODB_URI=mongodb://usuario:password@host1:27017,host2:27017,host3:27017/tvmaze?ssl=true&authSource=admin
```

Los nombres de los hosts se obtienen en Atlas, en *Connect → Drivers*, eligiendo una versión antigua del driver
(la que muestra la cadena sin `+srv`).

## Ejecutar

Con `MONGODB_URI` definida:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080` y la documentación interactiva (Swagger UI) en
[`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html). La especificación OpenAPI en JSON
está en `/v3/api-docs`.

La aplicación **no arranca** si falta `MONGODB_URI` o si MongoDB no está disponible (ver
[Decisiones de diseño](#decisiones-de-diseño)). Sin la variable, se detiene con este mensaje:

```
java.lang.IllegalStateException: Falta la variable de entorno MONGODB_URI. Defínela con la cadena de conexión de MongoDB (formato en .env.example; ver 'Configuración de MongoDB' en el README).
```

## Pruebas

```bash
mvn test
```

Las pruebas no necesitan MongoDB ni acceso a internet: los servicios se prueban con Mockito, el cliente de TV Maze con
`MockRestServiceServer` y los controladores con `@WebMvcTest` (MockMvc).

| Clase de prueba | Qué cubre |
|-----------------|-----------|
| `ShowControllerTest` | Búsqueda y detalle: 200, 400, 404, 502 y 503 |
| `CommentControllerTest` | Comentarios: 201, 400 con detalle por campo y 404 |
| `ShowServiceTest` | Búsqueda con comentarios (consulta única), show con comentarios sin modificar el caché |
| `ShowCacheServiceTest` | Cache hit, cache miss y show inexistente (no se guarda) |
| `CommentServiceTest` | Guardado de comentarios y agrupación por show |
| `TvMazeClientTest` | Mapeo de respuestas y errores HTTP de TV Maze |
| `ShowMapperTest` | Selección del canal y valores nulos |
| `CreateCommentRequestTest` | Reglas de validación del comentario |
| `MongoUriValidatorTest` | Mensaje claro al arrancar sin `MONGODB_URI` |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/shows/search?q={search_query}` | Busca shows por nombre |
| `GET` | `/api/shows/{showId}` | Obtiene un show completo con sus comentarios |
| `POST` | `/api/shows/{showId}/comments` | Guarda un comentario y una calificación |

Cada ejemplo se muestra para bash (macOS/Linux, Git Bash) y para PowerShell. En PowerShell se usa `curl.exe`
porque `curl` puede ser un alias de `Invoke-WebRequest`.

### A. Búsqueda de shows

`GET /api/shows/search?q={search_query}`

```bash
curl "http://localhost:8080/api/shows/search?q=girls"
```

```powershell
curl.exe "http://localhost:8080/api/shows/search?q=girls"
```

Respuesta `200 OK`:

```json
[
  {
    "id": 139,
    "name": "Girls",
    "channel": "HBO",
    "summary": "<p>This Emmy winning series is a comic look at...</p>",
    "genres": ["Drama", "Romance"],
    "comments": [
      { "comment": "Muy buena serie", "rating": 4 }
    ]
  },
  {
    "id": 525,
    "name": "Gilmore Girls",
    "channel": "The CW",
    "summary": "<p>...</p>",
    "genres": ["Drama", "Comedy", "Romance"],
    "comments": []
  }
]
```

- `channel` toma el nombre de `network` y, si el show no tiene cadena de TV, el de `webChannel`.
- `comments` contiene los comentarios guardados de cada show, del más antiguo al más reciente, o un arreglo vacío si
  no tiene.
- Si no hay resultados, la respuesta es `[]`.

### B. Show por ID

`GET /api/shows/{showId}`

```bash
curl "http://localhost:8080/api/shows/139"
```

```powershell
curl.exe "http://localhost:8080/api/shows/139"
```

Respuesta `200 OK`: el objeto show completo tal como lo entrega TV Maze (`GET https://api.tvmaze.com/shows/{id}`),
sin omitir ningún campo, más un arreglo `comments` (vacío si no tiene):

```json
{
  "id": 139,
  "url": "https://www.tvmaze.com/shows/139/girls",
  "name": "Girls",
  "type": "Scripted",
  "language": "English",
  "genres": ["Drama", "Romance"],
  "status": "Ended",
  "network": { "id": 8, "name": "HBO", "...": "..." },
  "...": "resto de los campos de TV Maze",
  "comments": [
    { "comment": "Muy buena serie", "rating": 4 }
  ]
}
```

La primera consulta de un show va a TV Maze y guarda el resultado en la colección `shows`; las siguientes se responden
desde MongoDB. Los comentarios se consultan en cada petición, así que siempre están actualizados aunque el show venga
del caché.

En consola, cada *cache miss* se registra con nivel INFO. Para ver también los *cache hit* (nivel DEBUG):

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.evaluacion.tvmaze.service=DEBUG
```

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--logging.level.com.evaluacion.tvmaze.service=DEBUG"
```

### C. Comentarios de un show

`POST /api/shows/{showId}/comments`

```bash
curl -X POST "http://localhost:8080/api/shows/139/comments" \
  -H "Content-Type: application/json" \
  -d '{ "comment": "Muy buena serie", "rating": 4 }'
```

```powershell
$body = @{ comment = "Muy buena serie"; rating = 4 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/shows/139/comments" `
  -ContentType "application/json; charset=utf-8" -Body $body
```

> Con `Invoke-RestMethod` no hay que escapar comillas: el JSON se genera con `ConvertTo-Json`, así que funciona igual
> en Windows PowerShell 5.1 y en PowerShell 7.

| Campo | Regla |
|-------|-------|
| `comment` | Obligatorio, no vacío, máximo 500 caracteres |
| `rating` | Entero obligatorio entre 0 y 5 (un decimal como `4.5` se rechaza) |

Respuesta `201 Created`:

```json
{
  "status": "success",
  "message": "Comentario guardado",
  "id": "66f0c2a1e4b0a1b2c3d4e5f6"
}
```

Antes de guardar se verifica que el show exista en TV Maze (usando el caché de shows). Cada comentario se guarda en la
colección `comments` con `id`, `showId`, `comment`, `rating` y `createdAt`; la colección tiene un índice sobre
`showId`.

## Errores

Todos los errores se devuelven en formato [ProblemDetail (RFC 7807)](https://www.rfc-editor.org/rfc/rfc7807) con
`Content-Type: application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No existe un show con ID 999999999",
  "instance": "/api/shows/999999999"
}
```

Los errores de validación incluyen además el detalle por campo en `errors`:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "La petición contiene campos inválidos",
  "instance": "/api/shows/139/comments",
  "errors": {
    "comment": "es obligatorio y no puede estar vacío",
    "rating": "debe ser menor o igual a 5"
  }
}
```

| Código | Caso | Endpoints |
|--------|------|-----------|
| 400 | Falta el parámetro `q` o está vacío | A |
| 400 | `showId` no es un número entero positivo | B, C |
| 400 | El cuerpo no es JSON válido, tiene tipos incorrectos o no cumple las validaciones | C |
| 404 | TV Maze no tiene un show con ese `showId` | B, C |
| 502 | TV Maze no respondió o respondió con error | A, B, C |
| 503 | MongoDB no está disponible | A, B, C |

## Estructura del proyecto

```
src/main/java/com/evaluacion/tvmaze
├── controller/   Endpoints REST (ShowController, CommentController)
├── service/      Lógica de negocio (ShowService, ShowCacheService, CommentService)
├── client/       Consumo del API de TV Maze (TvMazeClient)
│   └── dto/      Respuestas de TV Maze que usa la búsqueda
├── document/     Documentos de MongoDB (CachedShow, ShowComment)
├── repository/   Repositorios de Spring Data MongoDB
├── dto/          Peticiones y respuestas de la API
├── mapper/       Conversión entre modelos de TV Maze, documentos y DTOs
├── exception/    Excepciones y manejo global de errores (GlobalExceptionHandler)
└── config/       RestClient, reloj, propiedades, OpenAPI y validación de MONGODB_URI
```

## Decisiones de diseño

**Caché con un documento propio (`CachedShow`).** El show de TV Maze no se guarda como documento raíz, sino dentro de
`CachedShow(id, data, cachedAt)`. El `_id` es el `showId` de forma explícita, lo que evita ambigüedad entre el campo
`id` de TV Maze y el `_id` de Mongo. `data` conserva el show completo como `Map`, para no perder campos, y `cachedAt`
permite agregar expiración más adelante. El caché guarda solo datos de TV Maze: los comentarios se agregan a una copia
en cada respuesta.

**Una sola consulta para los comentarios de la búsqueda.** En lugar de consultar los comentarios de cada show (problema
N+1), la búsqueda reúne los IDs de todos los resultados y hace una sola consulta `findByShowIdIn` (`$in`), que usa el
índice de `showId`. Después agrupa los comentarios por show en memoria.

**`ShowCacheService` separado de `ShowService`.** `CommentService` necesita verificar que un show exista, y
`ShowService` necesita los comentarios. Si ambos dependieran uno del otro habría una dependencia circular. La lógica de
caché vive en `ShowCacheService`, del que dependen los otros dos:

```
ShowService ──► CommentService ──► ShowCacheService ──► TvMazeClient + CachedShowRepository
     └──────────────────────────────────┘
```

Así, además, guardar un comentario valida el show sin cargar comentarios que no se usan.

**Fail-fast sin MongoDB.** La cadena de conexión solo se toma de `MONGODB_URI`, sin un valor por defecto. Si falta, la
aplicación se detiene con un mensaje claro (`MongoUriValidator`, registrado en `main()` para que solo corra al levantar
la aplicación completa y no en las pruebas). Si MongoDB no está disponible, tampoco arranca, porque el índice de
`comments.showId` se crea al iniciar con `auto-index-creation`. Es preferible fallar al arrancar que conectarse por accidente a otra base de datos o fallar en
la primera petición. Si MongoDB se cae con la aplicación en marcha, los endpoints responden `503` en lugar de un `500`
genérico.

**Otras decisiones.**

- Errores con `ProblemDetail` y mensajes en español, centralizados en `GlobalExceptionHandler`.
- `Clock` inyectado para que `cachedAt` y `createdAt` sean deterministas en las pruebas.
- `accept-float-as-int: false` para que `"rating": 4.5` responda 400 en lugar de guardarse truncado como 4.
