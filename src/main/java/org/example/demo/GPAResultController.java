package org.example.demo;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class GPAResultController {

    @FXML private VBox coursesVBox;
    @FXML private Label gpaLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private Label resultDateLabel;
    @FXML private Label studentRollLabel;

    private ObservableList<Course> courseList;
    private String rollNumber;
    private ConcurrentGPACalculator gpaCalculator;

    public void setCourseListAndRoll(ObservableList<Course> courseList, String rollNumber) {
        this.courseList = courseList;
        this.rollNumber = rollNumber;
        this.gpaCalculator = new ConcurrentGPACalculator();
        displayResults();
    }

    // Keep backward compatibility
    public void setCourseList(ObservableList<Course> courseList) {
        this.courseList = courseList;
        this.rollNumber = courseList.isEmpty() ? "N/A" : courseList.get(0).getRollNumber();
        this.gpaCalculator = new ConcurrentGPACalculator();
        displayResults();
    }

    private void displayResults() {
        // Display roll number
        if (studentRollLabel != null) {
            studentRollLabel.setText("Student Roll Number: " + rollNumber);
        }

        // Clear previous content
        coursesVBox.getChildren().clear();

        // Use CompletableFuture for asynchronous GPA calculation
        CompletableFuture.supplyAsync(() -> {
            // Calculate GPA using parallel processing
            double gpa = gpaCalculator.calculateGPAParallel(new ArrayList<>(courseList));
            double totalCredits = gpaCalculator.getTotalCreditsParallel(new ArrayList<>(courseList));

            return new double[]{gpa, totalCredits};
        }).thenAcceptAsync(results -> {
            Platform.runLater(() -> {
                double gpa = results[0];
                double totalCredits = results[1];

                // Display GPA and credits
                gpaLabel.setText(String.format("%.2f", gpa));
                totalCreditsLabel.setText(String.format("%.1f", totalCredits));
            });
        });

        // Display each course
        for (int i = 0; i < courseList.size(); i++) {
            Course course = courseList.get(i);

            // Create course display box
            VBox courseBox = new VBox(5);
            courseBox.getStyleClass().add("course-result-box");

            Label courseHeader = new Label("Course " + (i + 1));
            courseHeader.getStyleClass().add("course-header");

            Label courseName = new Label("Course Name: " + course.getCourseName());
            Label courseCode = new Label("Course Code: " + course.getCourseCode());
            Label courseCredit = new Label("Credit Hours: " + course.getCourseCredit());
            Label teacher1 = new Label("Instructor 1: " + course.getTeacher1Name());
            Label teacher2 = new Label("Instructor 2: " + course.getTeacher2Name());
            Label grade = new Label("Grade: " + course.getGrade() + " (Grade Point: " +
                    String.format("%.2f", course.getGradePoint()) + ")");
            grade.getStyleClass().add("grade-label");

            courseBox.getChildren().addAll(courseHeader, courseName, courseCode,
                    courseCredit, teacher1, teacher2, grade);
            coursesVBox.getChildren().add(courseBox);
        }

        // Set current date
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        resultDateLabel.setText(currentDate.format(formatter));

        // Print statistics using concurrent calculator
        ConcurrentGPACalculator.GPAStatistics stats = gpaCalculator.getStatistics(new ArrayList<>(courseList));
        System.out.println("📊 GPA Statistics: " + stats);
    }

    @FXML
    private void goBackHome(ActionEvent event) {
        try {
            Parent homeRoot = FXMLLoader.load(getClass().getResource("Home.fxml"));
            Scene homeScene = new Scene(homeRoot, 1550, 800);
            homeScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(homeScene);
            window.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}