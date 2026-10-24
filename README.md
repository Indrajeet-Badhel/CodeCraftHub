# CodeCraftHub

CodeCraftHub is a beginner-friendly REST API for tracking personal learning courses. It uses Node.js, Express, and a local `courses.json` file—no database or account is required.

## Features

- Create, list, retrieve, update, and delete courses
- Automatically creates `courses.json` on first launch
- Numeric IDs start at 1 and increase automatically
- Validates required fields, dates, statuses, JSON requests, and IDs
- Stores data in a readable JSON file

Allowed statuses: `Not Started`, `In Progress`, and `Completed`.

## Install and run

1. Install [Node.js](https://nodejs.org/).
2. In this project folder, run:

```bash
npm install
npm start
```

The API starts at `http://localhost:5000`. The data file is created as `courses.json` in the project root.

## API endpoints

### Create a course

`POST /api/courses`

```bash
curl -X POST http://localhost:5000/api/courses \
  -H "Content-Type: application/json" \
  -d '{"name":"Python Basics","description":"Learn Python fundamentals","target_date":"2026-12-31","status":"Not Started"}'
```

### Get courses

`GET /api/courses` returns `{ "count": number, "courses": [] }`.

```bash
curl http://localhost:5000/api/courses
curl http://localhost:5000/api/courses/1
```

The second command gets course 1.

### Course statistics

`GET /api/courses/stats` returns the total course count and a count for every supported status, including statuses with no courses.

```bash
curl http://localhost:5000/api/courses/stats
```

Example response:

```json
{
  "total_courses": 3,
  "courses_by_status": {
    "Not Started": 1,
    "In Progress": 1,
    "Completed": 1
  }
}
```

### Update a course

`PUT /api/courses/:id` requires all editable fields.

```bash
curl -X PUT http://localhost:5000/api/courses/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Python Basics","description":"Learn Python fundamentals","target_date":"2026-12-31","status":"In Progress"}'
```

### Delete a course

```bash
curl -X DELETE http://localhost:5000/api/courses/1
```

## Course format

```json
{
  "id": 1,
  "name": "Python Basics",
  "description": "Learn Python fundamentals",
  "target_date": "2026-12-31",
  "status": "Not Started",
  "created_at": "2026-10-24T00:00:00.000Z"
}
```

## Errors

Invalid input returns `400`, a missing course returns `404`, and a storage problem returns `500`. For example, `status` must exactly match one of the three allowed statuses and `target_date` must be a real `YYYY-MM-DD` date.

## Troubleshooting

- **`Cannot find module 'express'`:** run `npm install` from this project folder.
- **Port 5000 is already in use:** stop the other program using it, or start with a different port using PowerShell: `$env:PORT=5001; npm start`.
- **Data file error:** make sure `courses.json` contains valid JSON—an empty file can simply be `[]`.
- **Request body is ignored:** include `Content-Type: application/json` for POST and PUT requests.
