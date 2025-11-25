package org.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private TextField rollNumberField;

    @FXML
    private void startGPACalculator(ActionEvent event) {
        try {
            // Validate roll number input
            String rollNumber = rollNumberField.getText().trim();

            if (rollNumber.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information",
                        "Please enter your roll number before proceeding!");
                return;
            }

            // Load the CourseEntry page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseEntry.fxml"));
            Parent courseEntryRoot = loader.load();

            // Get the controller and pass the roll number
            CourseEntryController controller = loader.getController();
            controller.setRollNumber(rollNumber);

            Scene courseEntryScene = new Scene(courseEntryRoot, 1550, 800);
            courseEntryScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(courseEntryScene);
            window.show();
        } catch(Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load the course entry page. Please try again.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}