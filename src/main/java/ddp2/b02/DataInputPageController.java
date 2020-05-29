package ddp2.b02;

import connectivity.Connectivity;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DataInputPageController implements Initializable {
    private Stage primaryStage;
    private Scene compareScene;

    //Date
    private String date;

    //Set DB Connectivity
    private Connectivity connectivity = new Connectivity();
    private Connection connection = connectivity.getConnection();
    private Statement statement = connection.createStatement();

    // Data from DB
    private static HashMap<String, HashMap<Integer, Object[]>> data = new HashMap<String, HashMap<Integer, Object[]>>();

    // Content in accordion
    @FXML
    private VBox dataFood;

    @FXML
    private VBox dataHousing;

    @FXML
    private VBox dataHealthCare;

    @FXML
    private VBox dataAcademic;

    @FXML
    private VBox dataMisc;

    @FXML
    private VBox dataTransport;

    // Date Picker
    @FXML
    private DatePicker datePicker;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Choice Box
    @FXML
    private ChoiceBox<String> choiceBox;
    private String[] dataList = {
            "FOOD",
            "HOUSING",
            "HEALTHCARE",
            "ACADEMIC",
            "MISC",
            "TRANSPORT"
    };

    // Expense Value
    @FXML
    private TextField expenseValue;

    @FXML
    private TextArea expenseDescription;

    //SuccessMessage
    @FXML
    private Label message;

    public DataInputPageController() throws SQLException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Hide successMessage
        message.setVisible(false);

        //Set Date Picker
        datePicker.setValue(java.time.LocalDate.now());
        datePicker.setEditable(false);

        //Set Choice Box Items
        choiceBox.setItems(FXCollections.observableArrayList(this.dataList));
        choiceBox.setValue("FOOD");

        insertData();
    }

    // Set Stage
    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Get Scene
    public void getScene(Scene compareScene) {
        this.compareScene = compareScene;
    }

    // Change Page
    public void changePage(ActionEvent actionEvent) {
        this.primaryStage.setScene(this.compareScene);
    }

    // Add Item to DB
    public void addItem(ActionEvent actionEvent) throws SQLException {
        String choice = choiceBox.getValue();
        String description = expenseDescription.getText();
        int value = 0;
        try {
            value = Integer.parseInt(expenseValue.getText());
            if (!description.equals("")) {
                // Insert Day Row
                this.date = datePicker.getValue().toString();
                String createDay = String.format("INSERT IGNORE INTO `day`(`date`) VALUES ('%s')", this.date);
                statement.executeUpdate(createDay);

                // Insert Item
                String sql;
                sql = String.format("INSERT INTO `item`(`date`, `type`, `value`, `description`) VALUES ('%s','%s', %d , '%s')", this.date, choice, value, description);
                statement.executeUpdate(sql);
                showMessage("Successfully added item!");
                refresh();
                expenseDescription.clear();
                expenseValue.clear();
            } else {
                showMessage("Failed, Fill all fields!");
            }
        } catch (NumberFormatException e) {
            showMessage("Failed, Fill all fields!");
            System.out.println("Not number");
        }


    }

    public void showMessage(String messageText) {
        message.setText(messageText);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(ev ->  message.setVisible(false));
        message.setVisible(true);
        delay.play();
    }

    /**
     * Fetching data from DB
     *
     * @param type
     * @throws SQLException
     */
    public HashMap<Integer, Object[]> fetchData(String type) throws SQLException {

        HashMap<Integer, Object[]> item = new HashMap<Integer, Object[]>();

        this.date = datePicker.getValue().toString();

        // Create SELECT query
        String query = String.format("SELECT * FROM item WHERE date='%s' AND type='%s'", this.date, type);

        // Create result of query
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        // Fetching the data from result
        try {
            while (resultSet.next()) {
                int itemId = resultSet.getInt("item_id");
                String description = resultSet.getString("description");
                int value = resultSet.getInt("value");
                Object[] dataItem = new Object[]{value, description};
                item.put(itemId, dataItem);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        statement.close();
        return item;
    }

    /**
     * Create GUI for bills
     *
     * @param dataItem
     * @return
     */
    public VBox showExpenses(HashMap<Integer, Object[]> dataItem) {

        VBox data = new VBox();

        // Check if the data is empty
        if (dataItem.size() < 1) {
            Label noItem = new Label("It's Empty");
            data.getChildren().addAll(noItem);
        } else {
            for (Integer id : dataItem.keySet()) {
                // Add item to accordion content
                GridPane itemContainer = new GridPane();
                Label description = new Label(dataItem.get(id)[1].toString());
                Label value = new Label("Rp " + dataItem.get(id)[0].toString());
                Button delete = new Button("X");

                // Add class for styling
                delete.getStyleClass().add("delete-button");
                description.getStyleClass().add("description-box");
                value.getStyleClass().add("value-box");
                itemContainer.getStyleClass().add("item-container");

                // Add trigger event
                delete.setOnAction(event -> {
                    delete(id);
                });

                // Create space for display
                int row = 0;
                itemContainer.getColumnConstraints().add(new ColumnConstraints(500));
                itemContainer.getColumnConstraints().add(new ColumnConstraints((150)));
                itemContainer.getColumnConstraints().add(new ColumnConstraints(20));
                itemContainer.add(description, 0, row);
                itemContainer.add(value, 1, row);
                itemContainer.add(delete,2, row);
                row++;

                data.getChildren().add(itemContainer);
            }
        }
        return data;
    }

    /**
     * Refresh accordion content
     */
    public void refresh() {
        dataFood.getChildren().clear();
        dataTransport.getChildren().clear();
        dataMisc.getChildren().clear();
        dataAcademic.getChildren().clear();
        dataHealthCare.getChildren().clear();
        dataHousing.getChildren().clear();
        insertData();
    }

    /**
     * Fetch data from DB to hashmap
     * And display the data in bills
     */
    public void insertData() {
        try {
            data.put("FOOD", fetchData("FOOD"));
            data.put("HOUSING", fetchData("HOUSING"));
            data.put("HEALTHCARE", fetchData("HEALTHCARE"));
            data.put("ACADEMIC", fetchData("ACADEMIC"));
            data.put("MISC", fetchData("MISC"));
            data.put("TRANSPORT", fetchData("TRANSPORT"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // Create GUI bills in accordion
        dataFood.getChildren().add(showExpenses(data.get("FOOD")));
        dataHousing.getChildren().add(showExpenses(data.get("HOUSING")));
        dataHealthCare.getChildren().add(showExpenses(data.get("HEALTHCARE")));
        dataAcademic.getChildren().add(showExpenses(data.get("ACADEMIC")));
        dataMisc.getChildren().add(showExpenses(data.get("MISC")));
        dataTransport.getChildren().add(showExpenses(data.get("TRANSPORT")));
    }

    public void delete(int id) {
       String query = String.format("DELETE FROM item WHERE item_id=%d", id);
       try {
           Statement statement = connection.createStatement();
           statement.execute(query);
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       refresh();
    }
}
