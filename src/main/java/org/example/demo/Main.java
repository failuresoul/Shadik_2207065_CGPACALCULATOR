package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
            Scene scene = new Scene(root, 1550, 800);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            primaryStage.setTitle("Student GPA Calculator");
            primaryStage.setScene(scene);

            // Add shutdown hook for graceful cleanup
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("🔄 Application closing... cleaning up resources");

                // Shutdown executor service
                DatabaseExecutorService executorService = DatabaseExecutorService.getInstance();
                executorService.shutdown();

                // Close database connection
                DatabaseManager dbManager = DatabaseManager.getInstance();
                dbManager.closeConnection();

                System.out.println("✅ Application closed successfully!");
            });

            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}