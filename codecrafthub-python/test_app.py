"""Automated tests for the CodeCraftHub Flask API."""

import json
import tempfile
import unittest
from pathlib import Path

import app as application


class CourseApiTests(unittest.TestCase):
    def setUp(self):
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.original_data_file = application.DATA_FILE
        application.DATA_FILE = Path(self.temporary_directory.name) / "courses.json"
        self.client = application.app.test_client()
        self.payload = {
            "name": "Python Basics",
            "description": "Learn Python fundamentals",
            "target_date": "2026-12-31",
            "status": "Not Started",
        }

    def tearDown(self):
        application.DATA_FILE = self.original_data_file
        self.temporary_directory.cleanup()

    def test_complete_crud_flow(self):
        created = self.client.post("/api/courses", json=self.payload)
        self.assertEqual(created.status_code, 201)
        self.assertEqual(created.json["course"]["id"], 1)

        listed = self.client.get("/api/courses")
        self.assertEqual(listed.status_code, 200)
        self.assertEqual(listed.json["count"], 1)

        fetched = self.client.get("/api/courses/1")
        self.assertEqual(fetched.status_code, 200)
        self.assertEqual(fetched.json["name"], "Python Basics")

        updated_payload = {**self.payload, "status": "In Progress"}
        updated = self.client.put("/api/courses/1", json=updated_payload)
        self.assertEqual(updated.status_code, 200)
        self.assertEqual(updated.json["course"]["status"], "In Progress")

        deleted = self.client.delete("/api/courses/1")
        self.assertEqual(deleted.status_code, 200)
        self.assertEqual(self.client.get("/api/courses").json["count"], 0)

    def test_validation_and_not_found_errors(self):
        self.assertEqual(self.client.post("/api/courses", json={}).status_code, 400)
        invalid = {**self.payload, "status": "Paused"}
        self.assertEqual(self.client.post("/api/courses", json=invalid).status_code, 400)
        self.assertEqual(self.client.get("/api/courses/999").status_code, 404)
        self.assertEqual(self.client.get("/api/courses/invalid").status_code, 400)

    def test_statistics(self):
        self.client.post("/api/courses", json=self.payload)
        self.client.post("/api/courses", json={**self.payload, "name": "Flask", "status": "Completed"})

        response = self.client.get("/api/courses/stats")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json["total_courses"], 2)
        self.assertEqual(response.json["courses_by_status"]["Not Started"], 1)
        self.assertEqual(response.json["courses_by_status"]["Completed"], 1)
        self.assertEqual(response.json["courses_by_status"]["In Progress"], 0)

    def test_courses_are_written_to_json(self):
        self.client.post("/api/courses", json=self.payload)
        stored_courses = json.loads(application.DATA_FILE.read_text(encoding="utf-8"))
        self.assertEqual(stored_courses[0]["name"], "Python Basics")


if __name__ == "__main__":
    unittest.main()
