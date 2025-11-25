package org.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CourseEntryController implements Initializable {

    @FXML private TextField courseNameField;
    @FXML private TextField courseCodeField;
    @FXML private TextField courseCreditField;
    @FXML private TextField teacher1Field;
    @FXML private TextField teacher2Field;
    @FXML private ComboBox<String> gradeComboBox;

    @FXML private TextField totalCreditField;
    @FXML private Label currentCreditLabel;
    @FXML private Button calculateGPAButton;

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> nameColumn;
    @FXML private TableColumn<Course, String> codeColumn;
    @FXML private TableColumn<Course, Double> creditColumn;
    @FXML private TableColumn<Course, String> teacher1Column;
    @FXML private TableColumn<Course, String> teacher2Column;
    @FXML private TableColumn<Course, String> gradeColumn;

    // Observable list for JavaFX table (UI layer)
    private ObservableList<Course> courseList = FXCollections.observableArrayList();

    // Database manager instance
    private DatabaseManager dbManager;

    private double totalCreditsEntered = 0.0;
    private double requiredTotalCredits = 0.0;
    private String studentRollNumber;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database manager (Singleton pattern)
        dbManager = DatabaseManager.getInstance();

        // Initialize grade combo box
        gradeComboBox.setItems(FXCollections.observableArrayList(
                "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"
        ));

        // Set up table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("courseCredit"));
        teacher1Column.setCellValueFactory(new PropertyValueFactory<>("teacher1Name"));
        teacher2Column.setCellValueFactory(new PropertyValueFactory<>("teacher2Name"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));

        courseTable.setItems(courseList);
        calculateGPAButton.setDisable(true);

        // Set default total credits
        totalCreditField.setText("12.0");
    }

    /**
     * Set the roll number from Home page
     */
    public void setRollNumber(String rollNumber) {
        this.studentRollNumber = rollNumber;
        // Load existing courses for this roll number
        loadCoursesFromDatabase();
    }

    /**
     * Loads all courses from database for the current student
     */
    private void loadCoursesFromDatabase() {
        if (studentRollNumber == null || studentRollNumber.isEmpty()) {
            return;
        }

        // Clear current list
        courseList.clear();
        totalCreditsEntered = 0.0;

        // Fetch from database for this specific student
        var courses = dbManager.getCoursesByRollNumber(studentRollNumber);

        // Add to observable list
        courseList.addAll(courses);

        // Calculate total credits
        for (Course course : courses) {
            totalCreditsEntered += course.getCourseCredit();
        }

        // Set required credits from first load
        if (!courses.isEmpty() && requiredTotalCredits == 0.0) {
            requiredTotalCredits = Double.parseDouble(totalCreditField.getText());
        }

        // Update UI
        currentCreditLabel.setText(String.format("%.1f / %.1f", totalCreditsEntered, requiredTotalCredits));

        // Enable calculate button if credits met
        if (totalCreditsEntered >= requiredTotalCredits && requiredTotalCredits > 0) {
            calculateGPAButton.setDisable(false);
            totalCreditField.setDisable(true);
        }
    }

    @FXML
    private void addCourse() {
        try {
            // Validate inputs
            if (courseNameField.getText().isEmpty() || courseCodeField.getText().isEmpty() ||
                    courseCreditField.getText().isEmpty() || teacher1Field.getText().isEmpty() ||
                    gradeComboBox.getValue() == null) {

                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all required fields!");
                return;
            }

            // Set required total credits on first course addition
            if (courseList.isEmpty() && !totalCreditField.getText().isEmpty()) {
                requiredTotalCredits = Double.parseDouble(totalCreditField.getText());
            }

            double credit = Double.parseDouble(courseCreditField.getText());

            // Check if adding this course exceeds total credits
            if (totalCreditsEntered + credit > requiredTotalCredits) {
                showAlert(Alert.AlertType.WARNING, "Credit Limit Exceeded",
                        "Adding this course will exceed the total credit limit!");
                return;
            }

            // Create course object with roll number
            Course course = new Course(
                    studentRollNumber,
                    courseNameField.getText(),
                    courseCodeField.getText(),
                    credit,
                    teacher1Field.getText(),
                    teacher2Field.getText().isEmpty() ? "N/A" : teacher2Field.getText(),
                    gradeComboBox.getValue()
            );

            // ✅ INSERT INTO DATABASE
            boolean inserted = dbManager.insertCourse(course);

            if (inserted) {
                // Add to UI list
                courseList.add(course);
                totalCreditsEntered += credit;
                currentCreditLabel.setText(String.format("%.1f / %.1f", totalCreditsEntered, requiredTotalCredits));

                // Clear fields
                clearFields();

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course added to database successfully!");

                // Enable Calculate GPA button if total credits reached
                if (totalCreditsEntered >= requiredTotalCredits) {
                    calculateGPAButton.setDisable(false);
                    totalCreditField.setDisable(true);
                }
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numeric values for credits!");
        }
    }

    @FXML
    private void deleteCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            // ✅ DELETE FROM DATABASE
            boolean deleted = dbManager.deleteCourse(selectedCourse);

            if (deleted) {
                // Remove from UI list
                courseList.remove(selectedCourse);
                totalCreditsEntered -= selectedCourse.getCourseCredit();
                currentCreditLabel.setText(String.format("%.1f / %.1f", totalCreditsEntered, requiredTotalCredits));

                if (totalCreditsEntered < requiredTotalCredits) {
                    calculateGPAButton.setDisable(true);
                    totalCreditField.setDisable(false);
                }

                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Course deleted from database successfully!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to delete!");
        }
    }

    @FXML
    private void resetAll() {
        // ✅ DELETE ALL FROM DATABASE for this student
        boolean deleted = dbManager.deleteAllCoursesByRollNumber(studentRollNumber);

        if (deleted) {
            courseList.clear();
            totalCreditsEntered = 0.0;
            requiredTotalCredits = 0.0;
            currentCreditLabel.setText("0.0 / 0.0");
            calculateGPAButton.setDisable(true);
            totalCreditField.setDisable(false);
            totalCreditField.setText("12.0");
            clearFields();

            showAlert(Alert.AlertType.INFORMATION, "Reset", "All courses cleared from database!");
        }
    }

    @FXML
    private void calculateGPA(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GPAResult.fxml"));
            Parent gpaResultRoot = loader.load();

            GPAResultController controller = loader.getController();
            controller.setCourseListAndRoll(courseList, studentRollNumber);

            Scene gpaResultScene = new Scene(gpaResultRoot, 1550, 800);
            gpaResultScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(gpaResultScene);
            window.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        courseNameField.clear();
        courseCodeField.clear();
        courseCreditField.clear();
        teacher1Field.clear();
        teacher2Field.clear();
        gradeComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}