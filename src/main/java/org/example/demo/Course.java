package org.example.demo;

public class Course {
    private String courseName;
    private String courseCode;
    private double courseCredit;
    private String teacher1Name;
    private String teacher2Name;
    private String grade;
    private double gradePoint;

    public Course(String courseName, String courseCode, double courseCredit,
                  String teacher1Name, String teacher2Name, String grade) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.courseCredit = courseCredit;
        this.teacher1Name = teacher1Name;
        this.teacher2Name = teacher2Name;
        this.grade = grade;
        this.gradePoint = calculateGradePoint(grade);
    }

    private double calculateGradePoint(String grade) {
        switch(grade) {
            case "A+": return 4.00;
            case "A": return 3.75;
            case "A-": return 3.50;
            case "B+": return 3.25;
            case "B": return 3.00;
            case "B-": return 2.75;
            case "C+": return 2.50;
            case "C": return 2.25;
            case "C-": return 2.00;
            case "D+": return 1.75;
            case "D": return 1.50;
            case "F": return 0.00;
            default: return 0.00;
        }
    }

    public double getWeightedGradePoint() {
        return gradePoint * courseCredit;
    }

    // Getters
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public double getCourseCredit() { return courseCredit; }
    public String getTeacher1Name() { return teacher1Name; }
    public String getTeacher2Name() { return teacher2Name; }
    public String getGrade() { return grade; }
    public double getGradePoint() { return gradePoint; }

    // Setters
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseCredit(double courseCredit) { this.courseCredit = courseCredit; }
    public void setTeacher1Name(String teacher1Name) { this.teacher1Name = teacher1Name; }
    public void setTeacher2Name(String teacher2Name) { this.teacher2Name = teacher2Name; }
    public void setGrade(String grade) {
        this.grade = grade;
        this.gradePoint = calculateGradePoint(grade);
    }
}