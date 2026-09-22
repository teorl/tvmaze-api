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

### B. Show por ID

`GET /api/shows/{showId}`

```bash
curl "http://localhost:8080/api/shows/139"
```

Devuelve el objeto show completo tal como lo entrega TV Maze (`GET https://api.tvmaze.com/shows/{id}`), sin omitir ningún campo.

Los shows se guardan en caché en la colección `shows` de MongoDB (con `_id` = `showId`):
la primera consulta va a TV Maze y guarda el resultado; las siguientes se responden desde Mongo.

En consola, cada *cache miss* se registra con nivel INFO. Para ver también los *cache hit* (nivel DEBUG):

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.evaluacion.tvmaze.service=DEBUG
```

### Errores

Los errores se devuelven en formato [ProblemDetail (RFC 7807)](https://www.rfc-editor.org/rfc/rfc7807):

| Código | Caso |
|--------|------|
| 400 | Falta el parámetro `q` o está vacío |
| 400 | `showId` no es un número entero positivo |
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
