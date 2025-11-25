package org.example.demo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager handles all SQLite database operations
 * Implements Singleton pattern for single database connection
 */
public class DatabaseManager {
    // Singleton instance
    private static DatabaseManager instance;

    // Database connection
    private Connection connection;

    // Database file path
    private static final String DB_URL = "jdbc:sqlite:gpa_calculator.db";

    /**
     * Private constructor to prevent direct instantiation
     * Initializes database connection and creates tables
     */
    private DatabaseManager() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection to database file
            connection = DriverManager.getConnection(DB_URL);

            // Create tables if they don't exist
            createTables();

            System.out.println("✅ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }

    /**
     * Gets singleton instance of DatabaseManager
     * @return DatabaseManager instance
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Creates necessary database tables
     * Table: courses
     * Columns: id (auto-increment), roll_number, course_name, course_code, course_credit,
     *          teacher1_name, teacher2_name, grade, grade_point, created_at
     */
    private void createTables() {
        String createCoursesTable = """
            CREATE TABLE IF NOT EXISTS courses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                roll_number TEXT NOT NULL,
                course_name TEXT NOT NULL,
                course_code TEXT NOT NULL,
                course_credit REAL NOT NULL,
                teacher1_name TEXT NOT NULL,
                teacher2_name TEXT,
                grade TEXT NOT NULL,
                grade_point REAL NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            // Execute table creation
            stmt.execute(createCoursesTable);

            // Check if roll_number column exists, if not add it (migration for old databases)
            migrateDatabase();

            System.out.println("✅ Tables created/verified successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Failed to create tables!");
            e.printStackTrace();
        }
    }

    /**
     * Migrates old database schema to new schema with roll_number column
     */
    private void migrateDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Check if roll_number column exists
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(courses)");
            boolean hasRollNumber = false;

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("roll_number".equals(columnName)) {
                    hasRollNumber = true;
                    break;
                }
            }
            rs.close();

            // If roll_number column doesn't exist, we need to recreate the table
            if (!hasRollNumber) {
                System.out.println("⚠️ Old database schema detected. Migrating to new schema...");

                // Since SQLite doesn't support ADD COLUMN with NOT NULL for existing data,
                // we need to drop and recreate the table
                stmt.execute("DROP TABLE IF EXISTS courses");
                System.out.println("✅ Old table dropped. Creating new table with roll_number column...");

                String createNewTable = """
                    CREATE TABLE courses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        roll_number TEXT NOT NULL,
                        course_name TEXT NOT NULL,
                        course_code TEXT NOT NULL,
                        course_credit REAL NOT NULL,
                        teacher1_name TEXT NOT NULL,
                        teacher2_name TEXT,
                        grade TEXT NOT NULL,
                        grade_point REAL NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;
                stmt.execute(createNewTable);
                System.out.println("✅ Database migration completed successfully!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to migrate database!");
            e.printStackTrace();
        }
    }

    /**
     * Inserts a new course into database
     * @param course Course object to insert
     * @return true if insertion successful, false otherwise
     */
    public boolean insertCourse(Course course) {
        String sql = """
            INSERT INTO courses (roll_number, course_name, course_code, course_credit, 
                                teacher1_name, teacher2_name, grade, grade_point)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set parameters (? placeholders)
            pstmt.setString(1, course.getRollNumber());      // roll_number
            pstmt.setString(2, course.getCourseName());      // course_name
            pstmt.setString(3, course.getCourseCode());      // course_code
            pstmt.setDouble(4, course.getCourseCredit());    // course_credit
            pstmt.setString(5, course.getTeacher1Name());    // teacher1_name
            pstmt.setString(6, course.getTeacher2Name());    // teacher2_name
            pstmt.setString(7, course.getGrade());           // grade
            pstmt.setDouble(8, course.getGradePoint());      // grade_point

            // Execute insert
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Course inserted: " + course.getCourseName() + " for Roll: " + course.getRollNumber());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to insert course!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all courses from database
     * @return List of all courses
     */
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY created_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through result set
            while (rs.next()) {
                // Create Course object from database row
                Course course = new Course(
                        rs.getString("roll_number"),
                        rs.getString("course_name"),
                        rs.getString("course_code"),
                        rs.getDouble("course_credit"),
                        rs.getString("teacher1_name"),
                        rs.getString("teacher2_name"),
                        rs.getString("grade")
                );
                courses.add(course);
            }

            System.out.println("✅ Retrieved " + courses.size() + " courses");
        } catch (SQLException e) {
            System.err.println("❌ Failed to retrieve courses!");
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Retrieves courses for a specific roll number
     * @param rollNumber Student's roll number
     * @return List of courses for that student
     */
    public List<Course> getCoursesByRollNumber(String rollNumber) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE roll_number = ? ORDER BY created_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Course course = new Course(
                        rs.getString("roll_number"),
                        rs.getString("course_name"),
                        rs.getString("course_code"),
                        rs.getDouble("course_credit"),
                        rs.getString("teacher1_name"),
                        rs.getString("teacher2_name"),
                        rs.getString("grade")
                );
                courses.add(course);
            }

            System.out.println("✅ Retrieved " + courses.size() + " courses for Roll: " + rollNumber);
        } catch (SQLException e) {
            System.err.println("❌ Failed to retrieve courses for roll number!");
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Updates an existing course in database
     * @param oldCourse Original course to update
     * @param newCourse Updated course data
     * @return true if update successful, false otherwise
     */
    public boolean updateCourse(Course oldCourse, Course newCourse) {
        String sql = """
            UPDATE courses 
            SET course_name = ?, course_code = ?, course_credit = ?,
                teacher1_name = ?, teacher2_name = ?, grade = ?, grade_point = ?
            WHERE roll_number = ? AND course_code = ? AND course_name = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set new values
            pstmt.setString(1, newCourse.getCourseName());
            pstmt.setString(2, newCourse.getCourseCode());
            pstmt.setDouble(3, newCourse.getCourseCredit());
            pstmt.setString(4, newCourse.getTeacher1Name());
            pstmt.setString(5, newCourse.getTeacher2Name());
            pstmt.setString(6, newCourse.getGrade());
            pstmt.setDouble(7, newCourse.getGradePoint());

            // Set WHERE clause values
            pstmt.setString(8, oldCourse.getRollNumber());
            pstmt.setString(9, oldCourse.getCourseCode());
            pstmt.setString(10, oldCourse.getCourseName());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Course updated: " + newCourse.getCourseName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to update course!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a course from database
     * @param course Course to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteCourse(Course course) {
        String sql = "DELETE FROM courses WHERE roll_number = ? AND course_code = ? AND course_name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, course.getRollNumber());
            pstmt.setString(2, course.getCourseCode());
            pstmt.setString(3, course.getCourseName());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Course deleted: " + course.getCourseName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to delete course!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes all courses for a specific student
     * @param rollNumber Student's roll number
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAllCoursesByRollNumber(String rollNumber) {
        String sql = "DELETE FROM courses WHERE roll_number = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            pstmt.executeUpdate();
            System.out.println("✅ All courses deleted for Roll: " + rollNumber);
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Failed to delete courses!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes all courses from database
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAllCourses() {
        String sql = "DELETE FROM courses";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ All courses deleted!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Failed to delete all courses!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Closes database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to close database connection!");
            e.printStackTrace();
        }
    }

    /**
     * Gets total number of courses in database
     * @return number of courses
     */
    public int getCourseCount() {
        String sql = "SELECT COUNT(*) as count FROM courses";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get course count!");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets course count for specific roll number
     * @param rollNumber Student's roll number
     * @return number of courses for that student
     */
    public int getCourseCountByRollNumber(String rollNumber) {
        String sql = "SELECT COUNT(*) as count FROM courses WHERE roll_number = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get course count!");
            e.printStackTrace();
        }
        return 0;
    }
}