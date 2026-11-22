
package com.kafepos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class KafePosApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        showLogin();

        primaryStage.setTitle("KAFEPOS - Cafe Management System");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/LoginView.fxml"));

        Scene scene = new Scene(loader.load());
        Styles.ensure(scene);
        primaryStage.setScene(scene);
    }

    private void showMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MainView.fxml"));

        Scene scene = new Scene(loader.load());

        TableView<?> table = (TableView<?>) scene.lookup(".transaction-table");

        if (table != null) {
            table.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY);
            table.setMaxWidth(Double.MAX_VALUE);
        }

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}