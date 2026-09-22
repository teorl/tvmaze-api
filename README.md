# TV Maze API Middleware

API REST en Java (Spring Boot) que funciona como middleware de los servicios de [TV Maze](https://www.tvmaze.com/api).

## Requisitos

- Java 21
- Maven 3.9+
- Un cluster de [MongoDB Atlas](https://www.mongodb.com/atlas) (o cualquier instancia de MongoDB)

## Configuración de MongoDB

La aplicación lee la cadena de conexión de la variable de entorno `MONGODB_URI` y usa la base de datos `tvmaze`.
Las credenciales nunca se escriben en el código ni en archivos versionados.

En [`.env.example`](.env.example) hay una plantilla del formato:

```
MONGODB_URI=mongodb+srv://usuario:password@cluster/tvmaze
```

Spring Boot no lee archivos `.env` por sí solo, así que la variable debe existir en la terminal antes de ejecutar la aplicación.

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

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Pruebas

```bash
mvn test
```

Las pruebas unitarias no necesitan conexión a MongoDB.

## Endpoints

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

Respuesta:

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
  }
]
```

`channel` toma el nombre de `network` y, si el show no tiene cadena de TV, el de `webChannel`.

`comments` contiene los comentarios guardados de cada show, del más antiguo al más reciente, o un arreglo vacío
si no tiene. Los comentarios de todos los shows del resultado se obtienen con una sola consulta a MongoDB.

### B. Show por ID

`GET /api/shows/{showId}`

```bash
curl "http://localhost:8080/api/shows/139"
```

```powershell
curl.exe "http://localhost:8080/api/shows/139"
```

Devuelve el objeto show completo tal como lo entrega TV Maze (`GET https://api.tvmaze.com/shows/{id}`), sin omitir ningún campo.

Los shows se guardan en caché en la colección `shows` de MongoDB (con `_id` = `showId`):
la primera consulta va a TV Maze y guarda el resultado; las siguientes se responden desde Mongo.

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
| `rating` | Entero obligatorio entre 0 y 5 |

Respuesta `201 Created`:

```json
{
  "status": "success",
  "message": "Comentario guardado",
  "id": "66f0c2a1e4b0a1b2c3d4e5f6"
}
```

Antes de guardar se verifica que el show exista (usando el caché de shows). Los comentarios se guardan en la
colección `comments` con `id`, `showId`, `comment`, `rating` y `createdAt`, con un índice sobre `showId`.
El índice se crea al iniciar la aplicación, por lo que MongoDB debe estar disponible al arrancar.

### Errores

Los errores se devuelven en formato [ProblemDetail (RFC 7807)](https://www.rfc-editor.org/rfc/rfc7807).
Los errores de validación incluyen el detalle por campo en `errors`:

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

| Código | Caso |
|--------|------|
| 400 | Falta el parámetro `q` o está vacío |
| 400 | `showId` no es un número entero positivo |
| 400 | El cuerpo del comentario no es JSON válido o no cumple las validaciones |
| 404 | TV Maze no tiene un show con ese `showId` |
| 502 | TV Maze no respondió o respondió con error |
| 503 | MongoDB no está disponible |

## Estructura

```
controller/  Endpoints REST
service/     Lógica de negocio
client/      Consumo del API de TV Maze
mapper/      Conversión de respuestas de TV Maze a DTOs propios
dto/         Objetos de respuesta
document/    Documentos de MongoDB
repository/  Repositorios de Spring Data MongoDB
exception/   Excepciones y manejo global de errores
config/      Configuración (RestClient, reloj, propiedades)
```
