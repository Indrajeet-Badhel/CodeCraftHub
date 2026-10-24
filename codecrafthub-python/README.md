# CodeCraftHub — Flask API

CodeCraftHub is a beginner-friendly Python Flask REST API for tracking courses you want to learn. It stores data in a local JSON file, so no database, authentication, or user setup is required.

## Features

- Create, retrieve, update, and delete courses
- JSON-file storage in `courses.json`, automatically created when missing
- Numeric course IDs starting at 1
- Input validation for required fields, valid statuses, IDs, and real dates
- Clear `400`, `404`, and `500` error responses
- Bonus statistics endpoint for course status counts
- Automated tests using Python's built-in `unittest`

## Project structure

```text
codecrafthub-python/
├── app.py             # Flask application and API routes
├── courses.json       # Persisted course data
├── requirements.txt   # Python dependencies
├── test_app.py        # Automated endpoint tests
└── README.md          # Project guide
```

## Installation and startup

1. Install Python 3.9 or newer.
2. Open a terminal in this folder.
3. Install dependencies:

```bash
python -m pip install -r requirements.txt
```

4. Start the API:

```bash
python app.py
```

The API runs at `http://localhost:5000`.

## API documentation

Each course includes `id`, `name`, `description`, `target_date`, `status`, and `created_at`. Status must be `Not Started`, `In Progress`, or `Completed`; dates must use `YYYY-MM-DD`.

### Create a course

```bash
curl -X POST http://localhost:5000/api/courses -H "Content-Type: application/json" -d '{"name":"Python Basics","description":"Learn Python fundamentals","target_date":"2026-12-31","status":"Not Started"}'
```

### Read courses

```bash
curl http://localhost:5000/api/courses
curl http://localhost:5000/api/courses/1
```

### Update a course

`PUT` requires all editable fields.

```bash
curl -X PUT http://localhost:5000/api/courses/1 -H "Content-Type: application/json" -d '{"name":"Python Basics","description":"Learn Python fundamentals","target_date":"2026-12-31","status":"In Progress"}'
```

### Delete a course

```bash
curl -X DELETE http://localhost:5000/api/courses/1
```

### Course statistics (bonus)

```bash
curl http://localhost:5000/api/courses/stats
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

## Testing

Run the full automated test suite:

```bash
python -m unittest -v
```

The tests use a temporary data file, so they do not modify your real `courses.json`.

## Troubleshooting

- **`No module named flask`:** rerun `python -m pip install -r requirements.txt` from this folder.
- **Port 5000 is busy:** stop the other application using the port, or use another port in PowerShell: `$env:PORT=5001; python app.py`.
- **Invalid JSON error:** ensure request JSON is valid and that POST/PUT requests send `Content-Type: application/json`.
- **Storage error:** ensure the project directory is writable and `courses.json` contains a JSON array, such as `[]`.
