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
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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


    @FXML
    private DatePicker datePicker;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyy-MM-dd");

    //Choice Box
    @FXML
    private ChoiceBox<String> choiceBox;
    private String[] categoryList = {
      "FOOD",
      "HOUSING",
      "HEALTHCARE",
      "ACADEMIC",
      "MISC",
      "TRANSPORT"
    };

    //Expense Value
    @FXML
    private TextField expenseValue;

    @FXML
    private TextArea expenseDescription;

    //SuccessMessage
    @FXML
    private Label successMsg;

    public DataInputPageController() throws SQLException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Hide successMessage
        successMsg.setVisible(false);

        //Set Date Picker
        datePicker.setValue(java.time.LocalDate.now());

        //Set Choice Box Items
        choiceBox.setItems(FXCollections.observableArrayList(this.categoryList));
        choiceBox.setValue("FOOD");
    }

    //Set Stage
    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    //Get Scene
    public void getScene(Scene compareScene) {
        this.compareScene = compareScene;
    }

    //Change Page
    public void changePage(ActionEvent actionEvent) {
        this.primaryStage.setScene(this.compareScene);
    }

    //Add Item to DB
    public void addItem(ActionEvent actionEvent) throws SQLException {
        String choice = choiceBox.getValue();
        String description = expenseDescription.getText();
        int value = 0;
        try {
            value = Integer.parseInt(expenseValue.getText());
        } catch (NumberFormatException e) {
            System.out.println("Not number");
        }

        //Insert Day Row
        this.date = datePicker.getValue().toString();
        String createDay = String.format("INSERT IGNORE INTO `day`(`date`) VALUES ('%s')", this.date);
        statement.executeUpdate(createDay);

        //Insert Item
        String sql;
        sql = String.format("INSERT INTO `item`(`date`, `type`, `value`, `description`) VALUES ('%s','%s', %d , '%s')", this.date, choice, value, description);
        statement.executeUpdate(sql);
        showMessage();
    }

    public void showMessage() {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(ev ->  successMsg.setVisible(false));
        successMsg.setVisible(true);
        delay.play();
    }
}
