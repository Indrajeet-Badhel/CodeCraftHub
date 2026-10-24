"""CodeCraftHub: a beginner-friendly Flask REST API for learning courses."""

import json
import os
import tempfile
from datetime import datetime, timezone
from pathlib import Path

from flask import Flask, jsonify, request

app = Flask(__name__)
DATA_FILE = Path(__file__).with_name("courses.json")
VALID_STATUSES = ("Not Started", "In Progress", "Completed")
PORT = int(os.environ.get("PORT", "5000"))


def ensure_data_file():
    """Create the JSON data file with an empty list when it is missing."""
    if not DATA_FILE.exists():
        DATA_FILE.write_text("[]\n", encoding="utf-8")


def read_courses():
    """Read courses from disk and return them as a list."""
    try:
        ensure_data_file()
        contents = DATA_FILE.read_text(encoding="utf-8").strip()
        courses = [] if not contents else json.loads(contents)
        if not isinstance(courses, list):
            raise ValueError("courses.json must contain a JSON array")
        return courses
    except (OSError, json.JSONDecodeError, ValueError) as error:
        raise RuntimeError("Unable to read courses.json") from error


def write_courses(courses):
    """Save courses safely by replacing the data file only after a full write."""
    try:
        file_descriptor, temporary_name = tempfile.mkstemp(
            dir=DATA_FILE.parent, prefix="courses-", suffix=".tmp", text=True
        )
        with os.fdopen(file_descriptor, "w", encoding="utf-8") as temporary_file:
            json.dump(courses, temporary_file, indent=2)
            temporary_file.write("\n")
        os.replace(temporary_name, DATA_FILE)
    except OSError as error:
        raise RuntimeError("Unable to write courses.json") from error


def valid_date(value):
    """Return True only for real calendar dates in YYYY-MM-DD format."""
    if not isinstance(value, str):
        return False
    try:
        return datetime.strptime(value, "%Y-%m-%d").strftime("%Y-%m-%d") == value
    except ValueError:
        return False


def validate_course(data):
    """Return a list of helpful validation errors for course input."""
    errors = []
    if not isinstance(data.get("name"), str) or not data["name"].strip():
        errors.append("name is required")
    if not isinstance(data.get("description"), str) or not data["description"].strip():
        errors.append("description is required")
    if not data.get("target_date"):
        errors.append("target_date is required")
    elif not valid_date(data["target_date"]):
        errors.append("target_date must be a real date in YYYY-MM-DD format")
    if not data.get("status"):
        errors.append("status is required")
    elif data["status"] not in VALID_STATUSES:
        errors.append("status must be one of: " + ", ".join(VALID_STATUSES))
    return errors


def parse_course_id(value):
    """Convert a URL ID to a positive integer, or return None if invalid."""
    try:
        course_id = int(value)
    except (TypeError, ValueError):
        return None
    return course_id if course_id > 0 else None


def json_body():
    """Return a JSON object request body or a clear 400 response payload."""
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return None, (jsonify(error="Request body must contain valid JSON"), 400)
    return data, None


@app.get("/")
def home():
    return jsonify(message="Welcome to CodeCraftHub API")


@app.post("/api/courses")
def create_course():
    data, error_response = json_body()
    if error_response:
        return error_response
    errors = validate_course(data)
    if errors:
        return jsonify(error="Validation failed", details=errors), 400

    courses = read_courses()
    next_id = max((int(course.get("id", 0)) for course in courses), default=0) + 1
    course = {
        "id": next_id,
        "name": data["name"].strip(),
        "description": data["description"].strip(),
        "target_date": data["target_date"],
        "status": data["status"],
        "created_at": datetime.now(timezone.utc).isoformat(),
    }
    courses.append(course)
    write_courses(courses)
    return jsonify(message="Course created successfully", course=course), 201


@app.get("/api/courses")
def get_courses():
    courses = read_courses()
    return jsonify(count=len(courses), courses=courses)


@app.get("/api/courses/stats")
def get_course_stats():
    courses = read_courses()
    by_status = {status: 0 for status in VALID_STATUSES}
    for course in courses:
        if course.get("status") in by_status:
            by_status[course["status"]] += 1
    return jsonify(total_courses=len(courses), courses_by_status=by_status)


@app.get("/api/courses/<course_id>")
def get_course(course_id):
    parsed_id = parse_course_id(course_id)
    if parsed_id is None:
        return jsonify(error="Course ID must be a positive integer"), 400
    course = next((item for item in read_courses() if item.get("id") == parsed_id), None)
    if course is None:
        return jsonify(error="Course not found"), 404
    return jsonify(course)


@app.put("/api/courses/<course_id>")
def update_course(course_id):
    parsed_id = parse_course_id(course_id)
    if parsed_id is None:
        return jsonify(error="Course ID must be a positive integer"), 400
    data, error_response = json_body()
    if error_response:
        return error_response
    errors = validate_course(data)
    if errors:
        return jsonify(error="Validation failed", details=errors), 400

    courses = read_courses()
    index = next((i for i, item in enumerate(courses) if item.get("id") == parsed_id), None)
    if index is None:
        return jsonify(error="Course not found"), 404
    course = {
        **courses[index],
        "name": data["name"].strip(),
        "description": data["description"].strip(),
        "target_date": data["target_date"],
        "status": data["status"],
        "updated_at": datetime.now(timezone.utc).isoformat(),
    }
    courses[index] = course
    write_courses(courses)
    return jsonify(message="Course updated successfully", course=course)


@app.delete("/api/courses/<course_id>")
def delete_course(course_id):
    parsed_id = parse_course_id(course_id)
    if parsed_id is None:
        return jsonify(error="Course ID must be a positive integer"), 400
    courses = read_courses()
    index = next((i for i, item in enumerate(courses) if item.get("id") == parsed_id), None)
    if index is None:
        return jsonify(error="Course not found"), 404
    course = courses.pop(index)
    write_courses(courses)
    return jsonify(message="Course deleted successfully", course=course)


@app.errorhandler(RuntimeError)
def handle_storage_error(error):
    app.logger.error("Storage error: %s", error)
    return jsonify(error=str(error)), 500


@app.errorhandler(404)
def handle_unknown_route(error):
    return jsonify(error="Route not found"), 404


if __name__ == "__main__":
    ensure_data_file()
    print("CodeCraftHub API is starting...")
    print(f"Data will be stored in: {DATA_FILE}")
    print(f"API will be available at: http://localhost:{PORT}")
    app.run(host="127.0.0.1", port=PORT, debug=False)
