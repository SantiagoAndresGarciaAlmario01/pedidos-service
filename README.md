# Pedidos Service — Arepa Express

Microservicio REST que recibe pedidos de Arepa Express, genera un identificador y publica el evento `pedido.creado` en RabbitMQ para que otros servicios lo procesen.

## Arquitectura del laboratorio

Durante el laboratorio se desplegaron los servicios en Azure Container Apps, RabbitMQ en Azure Container Instances y el API Gateway con Azure API Management (plan Consumption). El gateway fue la entrada única para el cliente y protegió las APIs con una clave de suscripción.

| Operación pública en el gateway | Servicio de destino |
|---|---|
| `GET /menu-api/menu` | `menu-service`, `GET /menu` |
| `POST /pedidos-api/pedidos` | `pedidos-service`, `POST /pedidos` |

Las solicitudes al gateway incluyen el encabezado `Ocp-Apim-Subscription-Key`. Sin la clave, el gateway responde `401`; con una clave válida, enruta la solicitud al servicio correspondiente. Una política de salida añade el encabezado `X-Arepa-Gateway: DOSW Company` a las respuestas.

### Eventos con RabbitMQ

Al aceptar un pedido, `pedidos-service` publica un evento JSON en el exchange topic `arepas.eventos`, con la clave de enrutamiento `pedido.creado`. Los consumidores procesan el evento de forma asíncrona:

| Cola | Consumidor | Clave de enlace |
|---|---|---|
| `cocina.pedidos` | `cocina-service` | `pedido.*` |
| `notificaciones.pedidos` | `notificaciones-service` | `pedido.creado` |
| `auditoria.pedidos` | Cola de auditoría creada para el experimento | `pedido.#` |

El evento contiene `pedidoId`, `cliente`, `arepas` y `fecha`. `cocina-service` registra el inicio y fin de la preparación; `notificaciones-service` registra la confirmación del pedido.

### Repositorios del proyecto

- [menu-service](https://github.com/SantiagoAndresGarciaAlmario01/menu-service)
- [pedidos-service](https://github.com/SantiagoAndresGarciaAlmario01/pedidos-service)
- [cocina-service](https://github.com/SantiagoAndresGarciaAlmario01/cocina-service)
- [notificaciones-service](https://github.com/SantiagoAndresGarciaAlmario01/notificaciones-service)

La infraestructura de Azure se eliminó al finalizar las pruebas del laboratorio; los endpoints de Azure usados durante el ejercicio ya no están activos.

## Tecnologías

- Java 21
- Spring Boot 3.5.0
- Spring Web
- Spring AMQP
- Maven
- RabbitMQ
- Docker

## Requisitos

- JDK 21
- Maven 3.9 o compatible
- RabbitMQ disponible para enviar eventos
- Docker Desktop (opcional, para ejecutar el contenedor)

## Configuración de RabbitMQ

La aplicación lee estas variables de entorno:

| Variable | Valor predeterminado | Descripción |
|---|---|---|
| `RABBIT_HOST` | `localhost` | Host del servidor RabbitMQ |
| `RABBIT_USER` | `arepas` | Usuario de RabbitMQ |
| `RABBIT_PASS` | `arepas123` | Contraseña de RabbitMQ |

El puerto AMQP utilizado es `5672`. Para otros entornos, configura las credenciales mediante variables de entorno.

## Ejecutar localmente

Con RabbitMQ disponible, desde la carpeta `pedidos-service` inicia la aplicación:

```bash
mvn spring-boot:run
```

La aplicación escucha en el puerto `8080`.

## API

### Crear un pedido

```http
POST /pedidos
Content-Type: application/json
```

Cuerpo de ejemplo:

```json
{
  "cliente": "Laura",
  "arepas": ["choclo", "huevo"]
}
```

Respuesta de ejemplo (HTTP `201 Created`):

```json
{
  "pedidoId": "identificador-generado",
  "estado": "RECIBIDO"
}
```

Ejemplo con `curl`:

```bash
curl -i -X POST http://localhost:8080/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cliente":"Laura","arepas":["choclo","huevo"]}'
```

## Mensajería

Al recibir un pedido, el servicio publica un evento JSON en el exchange topic `arepas.eventos`, usando la clave de enrutamiento `pedido.creado`. Los servicios consumidores pueden enlazar colas a este exchange para procesar los pedidos de forma asíncrona.

## Compilar

```bash
mvn -q package -DskipTests
```

El archivo JAR se genera dentro de `target/`.

## Construir y ejecutar con Docker

Primero compila el JAR y, desde esta carpeta, construye la imagen:

```bash
mvn -q package -DskipTests
docker build -t pedidos-service .
docker run --rm -p 8080:8080 \
  -e RABBIT_HOST=host.docker.internal \
  -e RABBIT_USER=arepas \
  -e RABBIT_PASS=arepas123 \
  pedidos-service
```

Para ejecutar el contenedor en este laboratorio, configura `RABBIT_HOST` con la dirección del RabbitMQ desplegado en Azure y usa las credenciales definidas para ese broker.

## Estructura principal

```text
pedidos-service/
├── src/main/java/com/arepa/pedidos/
│   ├── application/usecase/
│   ├── domain/model/
│   ├── domain/port/
│   └── infrastructure/
├── src/main/resources/application.properties
├── .dockerignore
├── .gitignore
├── Dockerfile
└── pom.xml
```
