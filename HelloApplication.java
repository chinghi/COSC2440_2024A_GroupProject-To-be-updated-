package com.example.cosc2440_2024a_groupproject;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class HelloApplication extends Application {

    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        // Connect to the database
        connectToDatabase();

        // Create UI components
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        TextArea operationsTextArea = new TextArea();
        operationsTextArea.setEditable(false);
        Button logoutButton = new Button("Logout");

        // Disable logout button initially
        logoutButton.setDisable(true);

        // Add event handler for login button
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Perform login in a background task
            Task<Boolean> loginTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    // Validate user credentials
                    return validateLogin(username, password);
                }
            };

            // Show progress indicator while logging in
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(20, 20);
            loginButton.setDisable(true);
            usernameField.setDisable(true);
            passwordField.setDisable(true);

            // After login task completes
            loginTask.setOnSucceeded(e -> {
                boolean loginSuccess = loginTask.getValue();

                if (loginSuccess) {
                    // Display list of available operations based on user's role
                    String role = getUserRole(username);
                    String operations = getOperationsForRole(role);
                    operationsTextArea.setText(operations);

                    // Enable logout button
                    logoutButton.setDisable(false);
                } else {
                    // Show error message if login failed
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid username or password. Please try again.");
                    alert.showAndWait();
                }

                // Hide progress indicator and enable UI components
                loginButton.setDisable(false);
                usernameField.setDisable(false);
                passwordField.setDisable(false);
                progressIndicator.setVisible(false);
            });

            // Start login task in a background thread
            new Thread(loginTask).start();

            // Show progress indicator
            progressIndicator.setVisible(true);
        });

        // Add event handler for logout button
        logoutButton.setOnAction(event -> {
            // Clear username and password fields
            usernameField.clear();
            passwordField.clear();

            // Clear operations text area
            operationsTextArea.clear();

            // Disable logout button
            logoutButton.setDisable(true);
        });

        // Layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, operationsTextArea, logoutButton);

        // Scene
        Scene scene = new Scene(root, 400, 300);

        // Set scene and show stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Insurance Claims Management System");
        primaryStage.show();
    }

    // Connect to the PostgreSQL database
    private void connectToDatabase() {
        String url = "jdbc:postgresql://localhost:5432/insurance_db";
        String user = "username";
        String password = "password";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Validate user credentials
    private boolean validateLogin(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get user's role
    private String getUserRole(String username) {
        try {
            String query = "SELECT role FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get available operations based on user's role
    private String getOperationsForRole(String role) {
        // Implement logic to determine operations based on user's role
        // For demonstration, we'll return fixed operations for each role
        switch (role) {
            case "PolicyHolder":
                return "1. File a claim\n2. Update personal information";
            case "Dependent":
                return "1. Retrieve claims\n2. Update personal information";
            case "PolicyOwner":
                return "1. File/update/delete claims\n2. Manage beneficiaries\n3. Calculate yearly payment";
            case "InsuranceSurveyor":
                return "1. Request more information for a claim\n2. Propose a claim to manager";
            case "InsuranceManager":
                return "1. Approve/reject claims\n2. Retrieve information of surveyors";
            case "SystemAdmin":
                return "1. CRUD operations for all entities\n2. Sum up successfully claimed amount";
            default:
                return "Unknown role";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
