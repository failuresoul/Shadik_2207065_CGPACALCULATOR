package org.example.demo;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private void startGPACalculator(ActionEvent event) {
        try {
            Parent courseEntryRoot = FXMLLoader.load(getClass().getResource("CourseEntry.fxml"));
            Scene courseEntryScene = new Scene(courseEntryRoot, 1550, 815);
            courseEntryScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(courseEntryScene);
            window.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}