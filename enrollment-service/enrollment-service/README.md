# Enrollment Service

Servicio de inscripciones de CampusEnroll HA.

## Puerto

- `8085`

## Endpoints

- `GET /health`
- `POST /api/enrollments`
- `GET /api/enrollments/{id}`
- `GET /api/students/{studentId}/enrollments`
- `DELETE /api/enrollments/{id}`

## Flujo de creacion de inscripcion

1. Valida estudiante activo en `student-service` (`GET /students/{id}/status`).
2. Valida seccion en `course-service` (`GET /sections/{id}`).
3. Valida duplicados para estados `PENDING_PAYMENT` y `CONFIRMED`.
4. Valida traslape de horarios (`GET /sections/{id}/schedule`).
5. Reserva cupo (`POST /sections/{id}/reserve-seat`).
6. Crea inscripcion en estado `PENDING_PAYMENT`.
7. Ejecuta cobro en `billing-service` (`POST /payments`).
8. Si pago `APPROVED`: confirma cupo y marca `CONFIRMED`.
9. Si pago `FAILED`: libera cupo y marca `PAYMENT_FAILED`.
10. Si falla billing/course despues de reservar: libera cupo y retorna error controlado.

## Base de datos

- Database: `campusenroll`
- Schema: `campusenroll`
- Local: `jdbc:postgresql://127.0.0.1:55432/campusenroll`
- Docker: `jdbc:postgresql://campusenroll-postgres:5432/campusenroll`

## Variables de integracion

- `STUDENT_SERVICE_URL` (default local: `http://localhost:8081`)
- `COURSE_SERVICE_URL` (default local: `http://localhost:8082`)
- `BILLING_SERVICE_URL` (default local: `http://localhost:8083`)

## Compilar

```bash
mvn clean compile
```