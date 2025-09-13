# CrediYa - Microservicio de Reportes

**Microservicio para generación y gestión de reportes de negocio implementando Clean Architecture con Spring Boot WebFlux**

## 📋 Descripción del Proyecto

CrediYa es una plataforma que digitaliza y optimiza la gestión de solicitudes de préstamos personales. Este microservicio maneja específicamente la **generación de reportes de negocio** (HU6 y HU7), proporcionando reportes consolidados de préstamos aprobados y enviando reportes diarios automáticos por email a los administradores del sistema.

### Funcionalidades Principales

- ✅ **Reportes Consolidados**: Generación de reportes con cantidad total y monto total de préstamos aprobados
- ✅ **Reportes Programados**: Envío automático diario de reportes de negocio por email
- ✅ **Envío Manual**: Endpoint para disparar manualmente el envío de reportes
- ✅ **Integración SQS**: Procesamiento reactivo de eventos de préstamos aprobados
- ✅ **Persistencia DynamoDB**: Almacenamiento de datos de reportes con AWS DynamoDB
- ✅ **Notificaciones Email**: Integración con AWS SES para envío de emails
- ✅ **API Reactiva**: Implementado con Spring Boot WebFlux para alta concurrencia
- ✅ **Seguridad JWT**: Autenticación y autorización con tokens JWT

## 🏗️ Arquitectura

### Clean Architecture (Hexagonal)

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

```
reportes-service/
├── applications/app-service/          # 🚀 Aplicación principal
├── domain/
│   ├── model/                         # 🏛️ Entidades del dominio
│   └── usecase/                       # 📋 Casos de uso
└── infrastructure/
    ├── driven-adapters/
    │   ├── dynamo-db/                 # 🗄️ Persistencia DynamoDB
    │   ├── jwt-security/              # 🔐 Autenticación y autorización JWT
    │   ├── sqs-listener/              # 📨 Consumidor de eventos SQS
    │   └── ses-email/                 # 📧 Servicio de email SES
    └── entry-points/
        ├── reactive-web/              # 🌐 API REST reactiva
        └── scheduled-tasks/           # ⏰ Tareas programadas
```

### Stack Tecnológico

- **Framework**: Spring Boot 3.5.4 con WebFlux (Programación Reactiva)
- **Base de Datos**: AWS DynamoDB con SDK v2 (NoSQL reactivo)
- **Mensajería**: AWS SQS para eventos de préstamos aprobados
- **Email**: AWS SES para notificaciones por correo
- **Seguridad**: JWT Authentication & Authorization con Spring Security
- **Programación**: Spring Scheduler para tareas automáticas
- **Mapeo**: MapStruct para conversión de DTOs
- **Validación**: Bean Validation con validadores personalizados
- **Documentación**: OpenAPI 3 / Swagger
- **Testing**: JUnit 5, Mockito, WebTestClient, StepVerifier
- **Calidad**: Jacoco (Coverage), SonarLint

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- AWS CLI configurado o LocalStack para desarrollo
- Gradle 8+

### Configuración

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd reportes-service
```

2. **Configurar AWS LocalStack (Desarrollo)**
```bash
# Iniciar LocalStack con Docker
docker run -d --name localstack -p 4566:4566 localstack/localstack

# Crear recursos AWS locales
aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name loan-reports \
    --attribute-definitions AttributeName=reportDate,AttributeType=S \
    --key-schema AttributeName=reportDate,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST

aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name loan-approved-events
```

3. **Configurar variables de entorno**
```bash
# .env (ejemplo)
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
AWS_REGION=us-east-1
DYNAMODB_ENDPOINT=http://localhost:4566
SQS_ENDPOINT=http://localhost:4566
SES_ENDPOINT=http://localhost:4566
JWT_SECRET=mySecretKey
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

4. **Ejecutar la aplicación**
```bash
# Compilar y ejecutar
./gradlew bootRun

# O ejecutar tests
./gradlew test

# Generar reporte de cobertura
./gradlew jacocoTestReport
```

## 📚 API Documentation

### Swagger UI
Una vez iniciada la aplicación, accede a la documentación interactiva:

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs

### Endpoints Principales

#### GET /api/v1/reportes
Obtener reporte consolidado de préstamos aprobados

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "reportDate": "2024-01-15",
  "totalApprovedLoans": 150,
  "totalApprovedAmount": 75000000,
  "generatedAt": "2024-01-15T10:30:00Z"
}
```

#### POST /api/v1/reportes/send-now
Enviar reporte diario manualmente

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "message": "Daily business report sent successfully"
}
```

### Validaciones y Seguridad

- **Autenticación**: Todos los endpoints requieren JWT válido
- **Autorización**: Solo usuarios con rol ASESOR pueden acceder
- **Validación**: Tokens JWT validados con clave secreta configurada

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
./gradlew test

# Tests específicos por módulo
./gradlew :domain:usecase:test
./gradlew :infrastructure:entry-points:reactive-web:test
./gradlew :infrastructure:driven-adapters:dynamo-db:test
./gradlew :infrastructure:driven-adapters:sqs-listener:test

# Con reporte de cobertura
./gradlew test jacocoTestReport
```

### Cobertura de Código
Los reportes se generan en:
- `build/reports/jacoco/test/html/index.html`
- Cobertura objetivo: >80%

## 📁 Estructura del Proyecto

### Domain Layer
- **`domain/model/`**: Entidades del dominio (LoanReport, LoanApprovedEvent)
- **`domain/usecase/`**: Lógica de negocio (LoanReportUseCase)

### Infrastructure Layer
- **`driven-adapters/dynamo-db/`**: Repositorio DynamoDB reactivo
- **`driven-adapters/jwt-security/`**: Filtros JWT y configuración de seguridad
- **`driven-adapters/sqs-listener/`**: Consumidor de eventos SQS
- **`driven-adapters/ses-email/`**: Cliente SES para envío de emails
- **`entry-points/reactive-web/`**: API REST reactiva con handlers y routers
- **`entry-points/scheduled-tasks/`**: Scheduler para reportes diarios automáticos

### Application Layer
- **`applications/app-service/`**: Configuración principal y punto de entrada

## 🔧 Configuración

El microservicio utiliza variables de entorno para configuración flexible:

- **Puerto**: 8082 (configurable con `SERVER_PORT`)
- **AWS**: Endpoints configurables para LocalStack o AWS real
- **DynamoDB**: Tabla `loan-reports` para persistencia
- **SQS**: Cola `loan-approved-events` para eventos
- **SES**: Configuración de email sender y destinatarios
- **JWT**: Clave secreta y configuración de validación
- **CORS**: Orígenes permitidos configurables

## 🔒 Seguridad

### Autenticación JWT
- **Filtro de Autenticación**: `JwtAuthenticationFilter` valida tokens JWT
- **Filtro de Autorización**: `RoleAuthorizationFilter` controla acceso por roles
- **Roles Soportados**: ADMIN, ASESOR
- **Endpoints Protegidos**: Todos los endpoints requieren autenticación

### Headers de Seguridad
- Content-Security-Policy
- Strict-Transport-Security
- X-Content-Type-Options
- Cache-Control

### CORS
Configurado para permitir orígenes específicos en desarrollo y producción.

## 📨 Integración con AWS

### DynamoDB
- **Tabla**: `loan-reports`
- **Clave**: `reportDate` (String)
- **Atributos**: `totalApprovedLoans`, `totalApprovedAmount`, `generatedAt`
- **Acceso**: SDK AWS v2 con cliente reactivo

### SQS
- **Cola**: `loan-approved-events`
- **Formato**: JSON con datos del préstamo aprobado
- **Procesamiento**: Reactivo con `@SqsListener`

### SES
- **Remitente**: Configurado via `SES_FROM_EMAIL`
- **Destinatarios**: Lista configurable de administradores
- **Formato**: HTML con datos del reporte consolidado

## ⏰ Tareas Programadas

### Reporte Diario Automático
- **Frecuencia**: Todos los días a las 8:00 AM
- **Función**: Genera y envía reporte consolidado por email
- **Configuración**: `@Scheduled(cron = "0 0 8 * * *")`
- **Contenido**: Cantidad y monto total de préstamos aprobados del día anterior

## 📊 Monitoreo y Logs

### Logs
- Trazabilidad completa de operaciones
- Manejo centralizado de excepciones
- Logs de eventos SQS procesados
- Logs de envío de emails

### Métricas
- Endpoints de salud disponibles
- Métricas de rendimiento reactivo
- Monitoreo de integración AWS

## 🚀 Despliegue

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY build/libs/reportes-service.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose
El servicio está configurado en `docker-compose.yml` con:
- Variables de entorno para AWS LocalStack
- Dependencias de LocalStack y MailHog
- Configuración JWT alineada con otros servicios

## 🤝 Contribución

### Estándares de Código
- SonarLint para validación
- Cobertura mínima: 80%
- Tests unitarios obligatorios
- Documentación de APIs con OpenAPI

### Git Flow
- Feature branches para nuevas funcionalidades
- Tests obligatorios antes de merge
- Revisión de código requerida
