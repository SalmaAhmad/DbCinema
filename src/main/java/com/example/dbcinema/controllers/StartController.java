package com.example.dbcinema.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.*;

public class StartController {

    @FXML
    private TableView<ObservableList<String>> table;
    @FXML
    private Button allmoviesbut;
    @FXML
    private Button addmov;
    @FXML
    private Button updmov;
    @FXML
    private Button deletemov;
    @FXML
    private TextField textbox;
    @FXML
    private TextField movtext;

    @FXML
    private TextField langtext;
    @FXML
    private TextField durtext;
    @FXML
    private TextField gentext;
    @FXML
    private TextField idtext;


    public void onLoadCustomersClicked(javafx.event.ActionEvent actionEvent) {
        String url = "jdbc:sqlserver://Jarvis:1433;databaseName=Cinemadb;user=User1;password=root;encrypt=true;trustServerCertificate=true";

        try (Connection conn = DriverManager.getConnection(url)) {
            String query = "SELECT * FROM Customer";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            table.getItems().clear(); // Clear old data

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            // Setting cell value factories for each column
            for (int i = 0; i < columnCount; i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> column = (TableColumn<ObservableList<String>, String>) table.getColumns().get(i);
                column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            }

            // Fetch rows
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            table.setItems(data);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addmovie(ActionEvent actionEvent) throws SQLException {
        String url = "jdbc:sqlserver://Jarvis:1433;databaseName=Cinemadb;user=User1;password=root;encrypt=true;trustServerCertificate=true";

        try (Connection conn = DriverManager.getConnection(url)) {
            //to get values from textfields
            String title = movtext.getText();
            String lang= langtext.getText();
            int dur= Integer.parseInt(durtext.getText());
            String genre = gentext.getText();


            String query = "INSERT INTO Movie ( Name, Language, Duration, Genre) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, title);
            stmt.setString(2, lang);
            stmt.setInt(3, dur);
            stmt.setString(4, genre);

            int rowsInserted = stmt.executeUpdate();

            conn.commit();

            if (rowsInserted > 0) {
                System.out.println("Movie added successfully!");
            } else {
                System.out.println("Failed to add movie.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updatemovie(ActionEvent actionEvent) {
        String url = "jdbc:sqlserver://Jarvis:1433;databaseName=Cinemadb;user=User1;password=root;encrypt=true;trustServerCertificate=true";

        try (Connection conn = DriverManager.getConnection(url)) {
            ObservableList<String> selectedRow = table.getSelectionModel().getSelectedItem();

            if (selectedRow == null) {
                System.out.println("No movie selected!");
                return;
            }

            int id = Integer.parseInt(selectedRow.get(0));
            //to get values from textfields
            String title = movtext.getText();
            String lang= langtext.getText();
            int dur= Integer.parseInt(durtext.getText());
            String genre = gentext.getText();



            String query = "UPDATE Movie SET Name = ?, Language = ?, Duration = ?, Genre = ? WHERE Movie_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, title);
            stmt.setString(2, lang);
            stmt.setInt(3, dur);
            stmt.setString(4, genre);
            stmt.setInt(5, id); //where condition

            int rowsInserted = stmt.executeUpdate();

            conn.commit();

            if (rowsInserted > 0) {
                System.out.println("Movie updated successfully!");
            } else {
                System.out.println("Failed to update movie.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletemovie(ActionEvent actionEvent) {
            String url = "jdbc:sqlserver://Jarvis:1433;databaseName=Cinemadb;user=User1;password=root;encrypt=true;trustServerCertificate=true";

            try (Connection conn = DriverManager.getConnection(url)) {
                // Get the Movie_ID to delete from the textfield
                ObservableList<String> selectedRow = table.getSelectionModel().getSelectedItem();

                if (selectedRow == null) {
                    System.out.println("No movie selected!");
                    return;
                }
                //int id = Integer.parseInt(idtext.getText());
                int id = Integer.parseInt(selectedRow.get(0));
                String query = "DELETE FROM Movie WHERE Movie_ID = ?";
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(1, id); // Bind Movie_ID

                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("Movie deleted successfully!");
                } else {
                    System.out.println("No movie found with the given ID.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



    public void showmovies(ActionEvent actionEvent) {
        String url = "jdbc:sqlserver://Jarvis:1433;databaseName=Cinemadb;user=User1;password=root;encrypt=true;trustServerCertificate=true";

        try (Connection conn = DriverManager.getConnection(url)) {
            String query = "SELECT * FROM Movie";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            table.getItems().clear(); // Clear old data

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            // Setting cell value factories for each column
            for (int i = 0; i < columnCount; i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> column = (TableColumn<ObservableList<String>, String>) table.getColumns().get(i);
                column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            }

            // Fetch rows
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            table.setItems(data);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
}
