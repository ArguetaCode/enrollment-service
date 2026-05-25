# enrollment-service

## Estado del compose local

- `docker-compose.yml` en esta carpeta esta **DEPRECADO**.
- El compose oficial del sistema esta en:
  - `campusenroll-ha/docker-compose.yml`
- Motivo:
  - El compose local conserva configuracion legacy (DBs separadas y puerto antiguo `8084`) y no representa el flujo oficial de Fase 1/Fase 2.

## Objetivo del servicio

Orquestar la inscripcion del estudiante en secciones con reglas de negocio distribuidas (validacion de estudiante, curso/seccion, pago y eventos).

## Responsabilidades

- Crear y consultar inscripciones.
- Invocar `student-service`, `course-service` y `billing-service`.
- Reaccionar a eventos de pago para actualizar estado de inscripcion.

## Endpoints esperados

- `GET /health`
- `POST /api/enrollments`
- `GET /api/enrollments/{id}`
- `GET /api/students/{studentId}/enrollments`
- `DELETE /api/enrollments/{id}`

## Modelo de datos esperado

- Tabla sugerida: `campusenroll.enrollments`
- Campos clave:
  - `id`
  - `student_id`
  - `section_id`
  - `status`
  - `payment_id`
  - `created_at`

## Estado actual

- Existe implementacion Spring Boot en `enrollment-service/enrollment-service`.
- Puerto objetivo ajustado a `8085` para evitar conflicto con notification.
- Usa RabbitMQ para eventos de pago.
- Estructura de package actual: `com.campusenroll.enrollment_service` (funciona, pero no es el estilo recomendado sin guion bajo).

## Pendientes

- Migrar package a `com.campusenroll.enrollment` (recomendado, no ejecutado para evitar riesgo de regresion).
- Homologar Spring Boot a `3.3.5` (actualmente usa `3.5.14`).
- Revisar estructura del repo para eliminar anidamiento redundante.
- Agregar pruebas de integracion end-to-end con pagos y notificaciones.
