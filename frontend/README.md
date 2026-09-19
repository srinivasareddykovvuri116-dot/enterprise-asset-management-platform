# Enterprise Multi-Tenant Asset & Task Management Platform

A full-stack enterprise application for managing organizations,
projects, tasks, users, analytics, and audit activity with strict
multi-tenant isolation and role-based access control.

## Overview

This platform is designed as a secure multi-tenant SaaS-style
application.

Each organization operates in an isolated workspace, while users receive
different permissions based on their role.

The system supports:

-   Organization management
-   User management
-   Role-based access control
-   Multi-tenant data isolation
-   Project management
-   Task management
-   Task filtering, sorting, and pagination
-   Dashboard analytics
-   Resource allocation analytics
-   Audit logging
-   JWT authentication
-   PostgreSQL persistence
-   Database migrations with Flyway
-   Automated integration testing
-   React-based enterprise dashboard

## Architecture


                    ┌──────────────────────┐
                    │      React UI        │
                    │   React + Vite       │
                    └──────────┬───────────┘
                               │
                               │ REST API
                               ▼
                    ┌──────────────────────┐
                    │   Spring Boot API    │
                    │                      │
                    │  Controllers         │
                    │  Services            │
                    │  Security            │
                    │  Tenant Context      │
                    └──────────┬───────────┘
                               │
                               │ JPA / Hibernate
                               ▼
                    ┌──────────────────────┐
                    │     PostgreSQL       │
                    │                      │
                    │ Organizations        │
                    │ Users                │
                    │ Projects             │
                    │ Tasks                │
                    │ Audit Logs           │
                    └──────────────────────┘


## Technology Stack

### Backend

-   Java 17
-   Spring Boot 4.0.8
-   Spring Security
-   Spring Data JPA
-   Hibernate
-   JWT
-   PostgreSQL
-   Flyway
-   Maven
-   JUnit 5
-   MockMvc

### Frontend

-   React 19
-   Vite
-   React Router
-   Redux Toolkit
-   Axios
-   Tailwind CSS
-   Recharts
-   Lucide React

### Development & Tooling

-   Git
-   Docker
-   Docker Compose
-   Maven Wrapper
-   npm

## Core Features

### Authentication

-   User registration
-   User login
-   JWT-based authentication
-   Password hashing
-   Protected API endpoints
-   Authentication failure handling

### Multi-Tenancy

Every organization operates within an isolated tenant boundary.

Tenant-aware authorization is applied throughout the backend to prevent
users from accessing resources belonging to another organization.

Examples include:

-   Projects
-   Tasks
-   Users
-   Analytics
-   Audit logs

### Role-Based Access Control

The application defines three roles:

  Role                   Access
  ---------------------- -----------------------------------------
  `ORGANIZATION_ADMIN`   Organization-wide administration
  `PROJECT_MANAGER`      Managed projects and related tasks
  `TEAM_MEMBER`          Assigned tasks and permitted operations

Authorization is enforced both in the frontend and backend.

The backend remains the authoritative security layer.

### Project Management

Supports:

-   Project creation
-   Project updates
-   Project archival
-   Project restoration
-   Project status management
-   Project manager assignment
-   Organization-level project isolation

### Task Management

Supports:

-   Task creation
-   Task updates
-   Task assignment
-   Status management
-   Priority management
-   Project association
-   Due dates
-   Filtering
-   Sorting
-   Pagination

### Analytics

Dashboard analytics include:

-   Total tasks
-   Completed tasks
-   Pending tasks
-   Overdue tasks
-   Tasks by status
-   Tasks by priority

Resource allocation analytics provide task distribution across team
members.

### Audit Logging

Important operations are recorded through an audit logging system.

Tracked operations include:

-   Organization creation
-   User creation
-   User activation
-   User suspension
-   Project creation
-   Project updates
-   Project archival
-   Task creation
-   Task updates
-   Task assignment
-   Task status changes

## Security Model


Client
  │
  ▼
JWT Authentication
  │
  ▼
Spring Security
  │
  ▼
Tenant Resolution
  │
  ▼
Role Authorization
  │
  ▼
Service-Level Authorization
  │
  ▼
Tenant-Scoped Database Query


This layered approach prevents relying solely on frontend authorization.

## Database

PostgreSQL is used as the primary database.

Main entities include:


Organization
    │
    ├── Users
    │
    └── Projects
            │
            └── Tasks
                    │
                    └── Assigned User

Organization
    │
    └── Audit Logs


Flyway manages database migrations.

Current migrations include:

-   `V1__baseline.sql`
-   `V2__add_project_organization_name_constraint.sql`

## Testing

The backend contains integration tests covering:

-   Analytics
-   Application startup
-   Project management
-   Security
-   Task management

Latest verification:


Tests run: 60
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS


Frontend verification:


npm run lint
PASS

npm run build
PASS


## Local Development

### Prerequisites

Install:

-   Java 17+
-   Maven or use the included Maven Wrapper
-   Node.js
-   npm
-   PostgreSQL 18+
-   Git

### Backend

Configure the required environment variables and database connection in
your local configuration.

Then run:

 powershell
.\mvnw spring-boot:run


Backend:


http://localhost:8080


### Frontend

 bash
cd frontend
npm install
npm run dev


Frontend:


http://localhost:5173


## Environment Variables

Create a local `.env` file where required.

Do not commit credentials or secrets.

Use the provided example configuration:


frontend/.env.example


## Project Structure


assetmanagement/
│
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── pages/
│   │   ├── routes/
│   │   └── store/
│   ├── package.json
│   └── .env.example
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/enterprise/assetmanagement/
│   │   │       ├── analytics/
│   │   │       ├── audit/
│   │   │       ├── config/
│   │   │       ├── dashboard/
│   │   │       ├── exception/
│   │   │       ├── organization/
│   │   │       ├── project/
│   │   │       ├── security/
│   │   │       ├── task/
│   │   │       ├── tenant/
│   │   │       └── user/
│   │   └── resources/
│   │       ├── db/migration/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/
│       └── resources/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md


## Engineering Highlights

This project demonstrates practical backend engineering concepts
including:

-   REST API design
-   JWT authentication
-   RBAC
-   Multi-tenant architecture
-   Service-layer authorization
-   PostgreSQL relational modeling
-   JPA/Hibernate
-   Database migrations
-   Dynamic query specifications
-   Pagination and sorting
-   Analytics queries
-   Audit logging
-   Integration testing
-   React state management
-   Production-oriented environment configuration

## Future Improvements

Potential future enhancements include:

-   Redis caching
-   Kafka-based event processing
-   Advanced notification system
-   Object storage for project assets
-   Kubernetes deployment
-   CI/CD pipeline
-   Cloud deployment
-   Distributed tracing
-   Metrics and observability

These are intentionally outside the current application scope.

## License

This project is intended as a software engineering portfolio project.
