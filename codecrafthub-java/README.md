# CodeCraftHub — Spring Boot API

CodeCraftHub is a beginner-friendly Java Spring Boot REST API for tracking learning courses. It uses a local `courses.json` file instead of a database, making it ideal for practicing REST API fundamentals.

## Features

- Create, list, retrieve, update, and delete courses
- JSON-file data storage that is created automatically
- Automatically generated numeric IDs
- Validates required fields, statuses, dates, and request JSON
- Helpful `400`, `404`, and `500` JSON error responses
- Bonus statistics endpoint
- Automated Spring Boot integration test

## Project structure

```text
codecrafthub-java/
├── pom.xml
├── courses.json
├── src/main/java/com/codecrafthub/
│   ├── CodeCraftHubApplication.java
│   ├── controller/
│   ├── model/
│   └── service/
├── src/main/resources/application.properties
└── src/test/java/com/codecrafthub/CourseApiIntegrationTest.java
```

## Requirements

- JDK 17 or newer
- Maven 3.6.3 or newer

## Build, test, and run

```bash
mvn clean test
mvn spring-boot:run
```

The server starts at `http://localhost:8080` and creates `courses.json` automatically if necessary.

## API endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/courses` | Create a course |
| GET | `/api/courses` | List all courses |
| GET | `/api/courses/{id}` | Get one course |
| PUT | `/api/courses/{id}` | Update one or more course fields |
| DELETE | `/api/courses/{id}` | Delete a course |
| GET | `/api/courses/stats` | Count courses by status |

### Create a course

```bash
curl -X POST http://localhost:8080/api/courses -H "Content-Type: application/json" -d '{"name":"Python Basics","description":"Learn Python fundamentals","target_date":"2026-12-31","status":"Not Started"}'
```

### Read courses

```bash
curl http://localhost:8080/api/courses
curl http://localhost:8080/api/courses/1
```

### Update a course status

```bash
curl -X PUT http://localhost:8080/api/courses/1 -H "Content-Type: application/json" -d '{"status":"In Progress"}'
```

### Delete a course

```bash
curl -X DELETE http://localhost:8080/api/courses/1
```

### Bonus statistics endpoint

```bash
curl http://localhost:8080/api/courses/stats
```

Example response:

```json
{
  "total_courses": 2,
  "courses_by_status": {
    "Not Started": 1,
    "In Progress": 0,
    "Completed": 1
  }
}
```

## Troubleshooting

- **`mvn` is not recognized:** install Maven and restart the terminal.
- **Port 8080 is busy:** stop the application using that port, or set `server.port=8081` in `src/main/resources/application.properties`.
- **Invalid JSON storage:** ensure `courses.json` contains a JSON array such as `[]`.
- **Tests fail after a forced stop:** delete `target/test-courses.json` and run `mvn clean test` again.
