# MediTurn

Sistema de gestión de turnos hospitalarios con arquitectura multitenante. Desarrollado con Java 21 + Spring Boot 3, React + TypeScript y PostgreSQL.

> **IA en roadmap** — integración con Claude API para reserva de turnos por lenguaje natural (Fase 2).

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security + JWT |
| Base de datos | PostgreSQL 16, Flyway, Redis |
| Frontend | React 18, TypeScript, TailwindCSS, React Query |
| Infra | Docker Compose, GitHub Actions |
| Docs | Swagger / OpenAPI |

## Arquitectura

El sistema es **multitenante** — una misma instancia sirve a múltiples organizaciones (hospitales, clínicas, etc.) con datos completamente aislados por `organization_id`.

```
React Frontend  →  Spring Boot API  →  PostgreSQL
                         ↓
                       Redis (cache disponibilidad)
```

## Levantar el proyecto

### Requisitos
- Docker y Docker Compose
- Java 21 (solo para desarrollo local sin Docker)

### Con Docker (recomendado)

```bash
docker-compose up -d
```

La API queda disponible en `http://localhost:8080`
Swagger UI en `http://localhost:8080/swagger-ui.html`

### Local (sin Docker para la app)

```bash
# 1. Levantar solo la base de datos y Redis
docker-compose up postgres redis -d

# 2. Correr la aplicación
./mvnw spring-boot:run
```

## Roles del sistema

| Rol | Descripción |
|---|---|
| `SUPER_ADMIN` | Gestiona todas las organizaciones |
| `ADMIN` | Configura su organización, médicos y servicios |
| `DOCTOR` | Ve su agenda y gestiona sus turnos |
| `RECEPTIONIST` | Carga y gestiona turnos manualmente |
| `PATIENT` | Reserva turnos y consulta su historial |

## Endpoints principales

```
POST   /api/auth/login
POST   /api/auth/register
GET    /api/appointments
POST   /api/appointments
PATCH  /api/appointments/{id}/confirm
PATCH  /api/appointments/{id}/cancel
GET    /api/availability/doctors/{id}
```

Ver documentación completa en Swagger UI.

## Migraciones de base de datos

Las migraciones se ejecutan automáticamente con Flyway al iniciar la app:

```
V1__create_organizations.sql
V2__create_users.sql
V3__create_appointments.sql
```

## Roadmap

- [x] Modelo de base de datos
- [x] Setup del proyecto y Docker
- [ ] Autenticación JWT
- [ ] CRUD de especialidades y médicos
- [ ] Motor de disponibilidad
- [ ] Reserva y gestión de turnos
- [ ] Panel de agenda
- [ ] Frontend React
- [ ] Integración Claude API (IA)

## Licencia

MIT
