package org.example.cinemadb;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class HelloController {
    @FXML
    private TableView<ObservableList<String>> table1;

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private Button gotostart;

    public void gotostart(javafx.event.ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("start.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void onLoadCustomersClicked(javafx.event.ActionEvent actionEvent) throws SQLException {
        try (Connection conn = SQLServerConnect.getConnection()) {
            String query = "SELECT * FROM Customer";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            assert table1 != null;
            table1.getItems().clear();            // Clear old rows
            table1.getColumns().clear();          // Clear old columns

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            // Dynamically create columns
            for (int i = 0; i < columnCount; i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(rsmd.getColumnName(i + 1));
                column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
                table1.getColumns().add(column);
            }

            // Add data
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            table1.setItems(data);
        }
    }

    public void addcustomer(ActionEvent actionEvent) {
    }

    public void updatecustomer(ActionEvent actionEvent) {
    }

    public void deletecustomer(ActionEvent actionEvent) {
    }
}
