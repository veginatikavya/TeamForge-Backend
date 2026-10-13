# TeamForge – Backend

### Constraint-Aware Skill-Based Team Formation and Project Capability Analysis Platform

TeamForge is a full-stack Java application designed to support intelligent project team formation based on verified employee skills, proficiency levels, availability, and project requirements.

This repository contains the **Spring Boot backend**, which provides REST APIs, authentication, role-based authorization, skill verification, candidate matching, team recommendations, and project capability analysis.

## Problem Statement

Manual project team formation can be inefficient because organizations must consider multiple constraints:

- Candidate skills and proficiency levels
- Reliability of self-reported skills
- Mandatory and optional project requirements
- Employee availability
- Project team-size limits
- Missing capabilities in existing teams
- Suitable replacements for assigned team members

TeamForge addresses these challenges through verified skill management, rule-based candidate evaluation, and skill-coverage analysis.

## Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 25 | Backend programming |
| Spring Boot | REST API development |
| Spring Web | HTTP request handling |
| Spring Data JPA | Database operations |
| Hibernate | Object-relational mapping |
| MySQL | Relational database |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| BCrypt | Password hashing |
| Maven | Dependency management and build automation |

## User Roles

TeamForge supports two primary roles.

### Administrator

Administrators can:

- Manage candidate profiles
- Manage projects and project requirements
- Review candidate-submitted skills
- Verify skills and approve proficiency levels
- Evaluate candidate matching results
- Form and manage project teams
- Generate team recommendations
- Analyze capability gaps
- Evaluate team replacements

### Candidate

Candidates can:

- Access their personal dashboard
- View their profile information
- Submit technical skills
- Specify claimed proficiency levels
- Update previously submitted skill proficiency
- View verification status

When a candidate updates an approved skill, the new proficiency claim requires administrator review.

## Core Backend Modules

### 1. Authentication and Authorization

- Authenticates registered users through the login API
- Uses BCrypt for password verification
- Generates JWT tokens for authenticated sessions
- Supports administrator and candidate roles
- Protects application APIs using token-based authentication

### 2. Candidate Management

- Creates and manages candidate profiles
- Stores preferred roles, experience, and availability
- Supports candidate information updates

### 3. Skill Management and Verification

- Maintains a centralized list of technical skills
- Stores candidate-submitted proficiency levels
- Supports updates to existing skill claims
- Tracks pending skill verification requests
- Allows administrators to approve proficiency levels
- Preserves approved proficiency while updated claims await review

### 4. Project Management

- Creates and manages projects
- Stores project descriptions and team-size limits
- Supports project skill requirements
- Maintains required proficiency levels and requirement priorities

### 5. Verified Skill-Based Candidate Matching

- Compares verified candidate skills against project requirements
- Calculates weighted matching scores
- Checks mandatory skill requirements
- Determines candidate eligibility
- Produces ranked candidate evaluation results
- Explains missing mandatory requirements

### 6. Team Formation

- Adds candidates to project teams
- Retrieves assigned team members
- Removes members from teams
- Validates project team-size constraints

### 7. Capability Gap Analysis

- Evaluates the capabilities of assigned team members
- Identifies missing or insufficient skill coverage
- Summarizes project skill gaps

### 8. Team Recommendations

- Identifies candidates who can improve team skill coverage
- Evaluates additional candidate contributions
- Produces recommendations based on project requirements

### 9. Automatic Team Formation

- Suggests team members based on skill coverage
- Considers candidate availability
- Uses verified skills and approved proficiency levels
- Respects project team-size limits
- Reports when complete coverage cannot be achieved

### 10. Team Replacement Analysis

- Evaluates potential replacements for existing team members
- Calculates replacement suitability scores
- Analyzes changes in project skill coverage
- Highlights remaining requirement gaps

## System Architecture

```text
React Frontend
      |
      | HTTP Requests / JSON
      | JWT Authentication
      v
Spring Boot REST Controllers
      |
      v
Service Layer
      |
      +-- Authentication and Authorization
      +-- Candidate Management
      +-- Skill Verification
      +-- Candidate Matching
      +-- Capability Gap Analysis
      +-- Team Recommendations
      +-- Automatic Team Formation
      +-- Team Replacement Analysis
      |
      v
Spring Data JPA Repositories
      |
      v
MySQL Database
```

## Candidate Matching Approach

TeamForge uses weighted skill-based evaluation.

Project skill requirements are classified into three categories:

| Requirement Priority | Weight |
|----------------------|--------|
| Mandatory | 3 |
| Preferred | 2 |
| Optional | 1 |

Candidate skill proficiency is compared against the proficiency required by the project.

The matching process considers:

1. Whether the candidate has an administrator-verified skill.
2. Whether the approved proficiency meets the project requirement.
3. The priority weight of the skill.
4. Whether mandatory requirements are satisfied.

A weighted matching score is calculated from the candidate's skill coverage.

**Important:** Candidate matching uses administrator-approved proficiency levels rather than unverified self-reported values.

A high matching score does not automatically mean that all mandatory requirements are satisfied. Mandatory eligibility is evaluated separately.

## Skill Verification Workflow

1. A candidate submits a skill and claimed proficiency level.
2. The skill becomes available for administrator review.
3. An administrator reviews the submission.
4. The administrator approves the proficiency level.
5. The approved proficiency becomes available for matching and team analysis.
6. If the candidate updates an approved skill, the updated claim requires another review.

Previously approved proficiency remains unchanged while an updated claim is pending.

This prevents unverified proficiency changes from directly influencing candidate matching results.

## Automatic Team Formation

Automatic team formation uses a greedy, skill-coverage-based approach.

The algorithm:

1. Evaluates available candidates.
2. Considers verified skills and approved proficiency levels.
3. Measures how much each candidate improves project skill coverage.
4. Selects candidates who provide useful additional coverage.
5. Repeats until the team-size limit is reached or no further improvement is available.
6. Reports incomplete coverage when necessary.

The generated team is a recommendation and is not automatically assigned to the project.

## Team Capability Analysis

TeamForge evaluates project requirements against the combined verified skills of assigned team members.

For each required skill, the application checks whether at least one assigned team member meets the required proficiency.

The analysis identifies:

- Fully covered skills
- Missing skills
- Skills with insufficient proficiency
- Overall team capability gaps

## REST API Overview

The backend runs locally on port `8081`.

Base API URL:

```text
http://localhost:8081/api
```

| Module | API Endpoint |
|--------|--------------|
| Authentication | `POST /api/auth/login` |
| Health Check | `GET /api/health` |
| Candidates | `/api/users` |
| Skills | `/api/skills` |
| Candidate Skills | `/api/user-skills` |
| Candidate Skill Submission | `POST /api/candidate/skills` |
| Projects | `/api/projects` |
| Project Requirements | `/api/project-requirements` |
| Team Members | `/api/team-members` |
| Candidate Matching | `GET /api/matching/project/{projectId}` |
| Capability Gaps | `GET /api/gaps/project/{projectId}` |
| Team Recommendations | `GET /api/team-recommendations/project/{projectId}` |
| Automatic Team Formation | `GET /api/automatic-team/project/{projectId}` |
| Team Replacement | `GET /api/team-replacement/project/{projectId}/member/{memberId}` |

Most application endpoints require a valid JWT token.

## Prerequisites

Install the following software:

- Java JDK 25
- MySQL Server
- An IDE such as Visual Studio Code or IntelliJ IDEA

The repository includes the Maven Wrapper, so a separate Maven installation is not required.

## Database Setup

### 1. Start MySQL

Ensure your local MySQL server is running.

### 2. Create the Database

Execute:

```sql
CREATE DATABASE IF NOT EXISTS teamforge_db;
```

### 3. Configure Database Access

The application uses the following configuration in:

```text
src/main/resources/application.properties
```

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/teamforge_db
spring.datasource.username=root
spring.datasource.password=${TEAMFORGE_DB_PASSWORD}
```

Set the database password in PowerShell:

```powershell
$env:TEAMFORGE_DB_PASSWORD = Read-Host "Enter your MySQL password"
```

Use the password configured for your local MySQL installation.

Do not store database passwords directly in source code or commit them to GitHub.

Hibernate is configured to update the database schema during development.

## JWT Configuration

The JWT secret is read from the environment variable:

```text
TEAMFORGE_JWT_SECRET
```

Generate and set a random secret in PowerShell:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$env:TEAMFORGE_JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Keep the JWT secret private.

The environment variable must be available in the terminal session used to start Spring Boot.

## Running the Backend

Open a terminal in the backend project directory.

On Windows, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend starts at:

```text
http://localhost:8081
```

The health endpoint is available at:

```text
http://localhost:8081/api/health
```

## Running the Complete Application

1. Start MySQL.
2. Set the `TEAMFORGE_DB_PASSWORD` environment variable.
3. Set the `TEAMFORGE_JWT_SECRET` environment variable.
4. Start the Spring Boot backend.
5. Start the React frontend separately using `npm run dev`.
6. Open the frontend at `http://localhost:5173`.
7. Log in and use the TeamForge modules.

## Future Enhancements

- Advanced optimization algorithms for team selection
- Candidate workload management
- Historical team performance analysis
- Reporting and export functionality
- Integration with external skill assessment systems
- Cloud deployment

## Project Information

**Project Name:** TeamForge

**Project Type:** Full Stack Java Application

**Frontend:** React + Vite

**Backend:** Java Spring Boot

**Database:** MySQL

**Purpose:** Constraint-aware skill-based team formation and project capability analysis.

## License

This project was developed for academic and educational purposes.