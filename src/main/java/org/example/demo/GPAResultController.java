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

public class GPAResultController {

    @FXML private VBox coursesVBox;
    @FXML private Label gpaLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private Label resultDateLabel;

    private ObservableList<Course> courseList;

    public void setCourseList(ObservableList<Course> courseList) {
        this.courseList = courseList;
        displayResults();
    }

    private void displayResults() {
        // Clear previous content
        coursesVBox.getChildren().clear();

        double totalWeightedPoints = 0.0;
        double totalCredits = 0.0;

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

            // Calculate totals
            totalWeightedPoints += course.getWeightedGradePoint();
            totalCredits += course.getCourseCredit();
        }

        // Calculate and display GPA
        double gpa = totalWeightedPoints / totalCredits;
        gpaLabel.setText(String.format("%.2f", gpa));
        totalCreditsLabel.setText(String.format("%.1f", totalCredits));

        // Set current date
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        resultDateLabel.setText(currentDate.format(formatter));
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