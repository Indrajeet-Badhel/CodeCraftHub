const express = require("express");
const fs = require("fs/promises");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 5000;
const DATA_FILE = path.join(__dirname, "courses.json");
const VALID_STATUSES = ["Not Started", "In Progress", "Completed"];

app.use(express.json());

// Create the storage file the first time the application runs.
async function ensureDataFile() {
  try {
    await fs.access(DATA_FILE);
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
    await fs.writeFile(DATA_FILE, "[]\n", "utf8");
  }
}

// Load the saved courses. An empty file is treated as an empty course list.
async function readCourses() {
  await ensureDataFile();
  const contents = await fs.readFile(DATA_FILE, "utf8");

  if (!contents.trim()) return [];

  const courses = JSON.parse(contents);
  if (!Array.isArray(courses)) {
    const error = new Error("courses.json must contain an array");
    error.code = "INVALID_DATA";
    throw error;
  }
  return courses;
}

// Write through a temporary file so courses.json is not left half-written.
async function writeCourses(courses) {
  const temporaryFile = `${DATA_FILE}.tmp`;
  await fs.writeFile(temporaryFile, `${JSON.stringify(courses, null, 2)}\n`, "utf8");
  await fs.rename(temporaryFile, DATA_FILE);
}

function isValidDate(value) {
  if (typeof value !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false;
  }

  const [year, month, day] = value.split("-").map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day;
}

function validateCourse(body) {
  body = body || {};
  const errors = [];

  if (typeof body.name !== "string" || !body.name.trim()) errors.push("name is required");
  if (typeof body.description !== "string" || !body.description.trim()) errors.push("description is required");
  if (body.target_date === undefined || body.target_date === null || body.target_date === "") {
    errors.push("target_date is required");
  } else if (!isValidDate(body.target_date)) {
    errors.push("target_date must be a real date in YYYY-MM-DD format");
  }
  if (!body.status) {
    errors.push("status is required");
  } else if (!VALID_STATUSES.includes(body.status)) {
    errors.push(`status must be one of: ${VALID_STATUSES.join(", ")}`);
  }

  return errors;
}

function parseId(value) {
  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : null;
}

function nextId(courses) {
  return courses.reduce((highest, course) => Math.max(highest, Number(course.id) || 0), 0) + 1;
}

app.get("/", (req, res) => {
  res.json({ message: "Welcome to CodeCraftHub API" });
});

app.post("/api/courses", async (req, res, next) => {
  try {
    const errors = validateCourse(req.body);
    if (errors.length) return res.status(400).json({ error: "Validation failed", details: errors });

    const courses = await readCourses();
    const course = {
      id: nextId(courses),
      name: req.body.name.trim(),
      description: req.body.description.trim(),
      target_date: req.body.target_date,
      status: req.body.status,
      created_at: new Date().toISOString()
    };
    courses.push(course);
    await writeCourses(courses);
    res.status(201).json({ message: "Course created successfully", course });
  } catch (error) {
    next(error);
  }
});

app.get("/api/courses", async (req, res, next) => {
  try {
    const courses = await readCourses();
    res.json({ count: courses.length, courses });
  } catch (error) {
    next(error);
  }
});

// This route must appear before /api/courses/:id, otherwise Express would
// interpret the word "stats" as a course ID.
app.get("/api/courses/stats", async (req, res, next) => {
  try {
    const courses = await readCourses();
    const byStatus = Object.fromEntries(
      VALID_STATUSES.map((status) => [status, 0])
    );

    for (const course of courses) {
      // Count only recognized values so a manually edited data file cannot
      // create an unexpected statistics category.
      if (Object.hasOwn(byStatus, course.status)) {
        byStatus[course.status] += 1;
      }
    }

    res.json({ total_courses: courses.length, courses_by_status: byStatus });
  } catch (error) {
    next(error);
  }
});

app.get("/api/courses/:id", async (req, res, next) => {
  try {
    const id = parseId(req.params.id);
    if (!id) return res.status(400).json({ error: "Course ID must be a positive integer" });

    const course = (await readCourses()).find((item) => item.id === id);
    if (!course) return res.status(404).json({ error: "Course not found" });
    res.json(course);
  } catch (error) {
    next(error);
  }
});

app.put("/api/courses/:id", async (req, res, next) => {
  try {
    const id = parseId(req.params.id);
    if (!id) return res.status(400).json({ error: "Course ID must be a positive integer" });

    const errors = validateCourse(req.body);
    if (errors.length) return res.status(400).json({ error: "Validation failed", details: errors });

    const courses = await readCourses();
    const index = courses.findIndex((item) => item.id === id);
    if (index === -1) return res.status(404).json({ error: "Course not found" });

    const course = {
      ...courses[index],
      name: req.body.name.trim(),
      description: req.body.description.trim(),
      target_date: req.body.target_date,
      status: req.body.status,
      updated_at: new Date().toISOString()
    };
    courses[index] = course;
    await writeCourses(courses);
    res.json({ message: "Course updated successfully", course });
  } catch (error) {
    next(error);
  }
});

app.delete("/api/courses/:id", async (req, res, next) => {
  try {
    const id = parseId(req.params.id);
    if (!id) return res.status(400).json({ error: "Course ID must be a positive integer" });

    const courses = await readCourses();
    const index = courses.findIndex((item) => item.id === id);
    if (index === -1) return res.status(404).json({ error: "Course not found" });

    const [course] = courses.splice(index, 1);
    await writeCourses(courses);
    res.json({ message: "Course deleted successfully", course });
  } catch (error) {
    next(error);
  }
});

app.use((req, res) => res.status(404).json({ error: "Route not found" }));

app.use((error, req, res, next) => {
  console.error(error);
  if (error instanceof SyntaxError && error.status === 400) {
    return res.status(400).json({ error: "Request body contains invalid JSON" });
  }
  if (error instanceof SyntaxError || error.code === "INVALID_DATA") {
    return res.status(500).json({ error: "courses.json contains invalid JSON" });
  }
  res.status(500).json({ error: "Unable to read or write courses.json" });
});

async function startServer() {
  try {
    await ensureDataFile();
    app.listen(PORT, () => {
      console.log("CodeCraftHub API is starting...");
      console.log(`Data will be stored in: ${DATA_FILE}`);
      console.log(`API is available at: http://localhost:${PORT}`);
    });
  } catch (error) {
    console.error("Could not start the server:", error.message);
    process.exitCode = 1;
  }
}

if (require.main === module) startServer();

module.exports = { app, ensureDataFile, readCourses, writeCourses };
