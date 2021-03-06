package ddp2.b02;

import connectivity.Connectivity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ComparePageController implements Initializable {
    private Stage primaryStage;
    private Scene dataInputScene;
    
    //Set DB Connectivity
    private Connectivity connectivity = new Connectivity();
    private Connection connection = (Connection) connectivity.getConnection();
    private Statement statement = connection.createStatement();

    // List of expenses category
    private String[] categoryList = {
        "FOOD",
        "HOUSING",
        "HEALTHCARE",
        "ACADEMIC",
        "MISC",
        "TRANSPORT"
    };

    // From date picker
    @FXML
    private DatePicker fromDatePick;

    // To date picker
    @FXML
    private DatePicker toDatePick;

    // Invalid date prompt
    @FXML
    private Label invalidDate;

    // Refresh button
    @FXML
    private Button refreshButton;

    // Line chart
    @FXML
    private LineChart lineChart;

    // Pie chart
    @FXML
    private PieChart pieChart;

    // No data found prompt
    @FXML
    private Label noData;

    // Go to summary page button
    @FXML
    private Button summaryButton;

    // Go to category page button
    @FXML
    private Button categoryButton;

    public ComparePageController() throws SQLException {
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void getScene(Scene dataInputScene) {
        this.dataInputScene = dataInputScene;
    }

    public void changePage(ActionEvent actionEvent) {
        this.primaryStage.setScene(this.dataInputScene);
    }

    /**
     * Will initialize to display summary page
     * Category page elements will be hidden
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Node settings
        fromDatePick.setEditable(false);
        toDatePick.setEditable(false);
        invalidDate.setVisible(false);
        pieChart.setLabelsVisible(false); // Hide labels
        pieChart.setLegendVisible(true);
        pieChart.setLegendSide(Side.RIGHT);
        summaryButton.setDisable(true); // Disable summary button
        pieChart.setVisible(false); // Hide pie chart
        noData.setVisible(false);

        /**
         * Generate the Line Graph for summary page
         */
        String todayDate = getTodayDate();
        int totalExpenses = getExpensesData();


        // Add today's expenses to the line chart
        XYChart.Series series = new XYChart.Series();
        series.setName("Today's Expenses");
        series.getData().add(new XYChart.Data(todayDate, totalExpenses));
        lineChart.getData().add(series);

        //Generate pie chart
        try {
            this.generatePieChart(this.pieChart);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getTodayDate() {
        return ((LocalDate) LocalDate.now()).toString();
    }

    public int getExpensesData() {
        // Query statement
        String todayDate = getTodayDate();
        String queryTodayData;
        queryTodayData = String.format("SELECT * FROM item WHERE date='%s'", todayDate);

        // Getting the data
        int totalExpenses = 0;
        try {
            ResultSet rs;
            rs = statement.executeQuery(queryTodayData);
            while (rs.next()) { // Iterate through all the data recieved
                totalExpenses += rs.getInt("value");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return totalExpenses;
    }

    public void generatePieChart(PieChart piechart) throws SQLException {

        /**
         * Generate the Pie Chart for summary page
         */
        // Check if data found or not
        boolean dataFound = false;

        // PieChart.Data array to store all expenses data per category
        PieChart.Data[] totalPerCategory = new PieChart.Data[categoryList.length];

        // Get total expenses per category
        for (int i = 0; i < categoryList.length; i++) {
            // Query statement
            String category = categoryList[i];
            String queryForCategory;
            queryForCategory = String.format("SELECT * FROM item WHERE type='%s'", category);

            // Getting the data
            int totalExpensesCategory = 0;
            try {
                ResultSet rs;
                rs = statement.executeQuery(queryForCategory);
                while (rs.next()) { // Iterate through all the data recieved
                    totalExpensesCategory += rs.getInt("value");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Add this category total expenses data to PieChart.Data array
            totalPerCategory[i] = new PieChart.Data(category, totalExpensesCategory);
            if (totalExpensesCategory != 0) { dataFound = true; } // Minimal one true value
        }

        // Generating the pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(totalPerCategory);
        pieChart.setData(pieChartData);
        pieChart.setTitle("Lifetime Expenses");
        if (dataFound) { noData.setVisible(false); } else { noData.setVisible(true); }
    }



    /**
     * Check if date is valid (from date is before to date)
     * Return true if invalid; false otherwise
     */
    public boolean isInvalidDate() {
        if ((fromDatePick.getValue() != null) && (toDatePick.getValue() != null) && (toDatePick.getValue().isBefore(fromDatePick.getValue()))) {
            invalidDate.setVisible(true); // Show invalid date prompt
            return true;
        } else { 
            invalidDate.setVisible(false);
            return false;
        }
    }


    
    /**
     * Event handler when from date was changed
     */
    public void onFromDateChange(ActionEvent actionEvent) {
        refreshButton.fire(); // Press the refresh button
    }



    /**
     * Event handler when to date was changed
     */
    public void onToDateChange(ActionEvent actionEvent) {
        refreshButton.fire(); // Press the refresh button
    }



    /**
     * Event handler on summary button press
     */
    public void gotoSummary(ActionEvent actionEvent) {
        categoryButton.setDisable(false); // Enable category button
        summaryButton.setDisable(true); // Disable summary button
        pieChart.setVisible(false); // Hide pie chart
        lineChart.setVisible(true); // Show line chart
        isInvalidDate(); // Show or hide invalid date prompt
    }

    /**
     * Event handler on category button press
     */
    public void gotoCategory(ActionEvent actionEvent) {
        categoryButton.setDisable(true); // Disable category button
        summaryButton.setDisable(false); // Enable summary button
        pieChart.setVisible(true); // Show pie chart
        lineChart.setVisible(false); // Hide line chart
        isInvalidDate(); // Show or hide invalid date prompt
    }



    /**
     * Event handler on refresh button press. 
     * Depends on what graph is showing.
     */
    public void refresh(ActionEvent actionEvent) {
        /**
         * Refresh Line Chart
         */
        if (lineChart.isVisible()) {
            // Get from and to dates
            LocalDate fromLocalDate = fromDatePick.getValue();
            LocalDate toLocalDate = toDatePick.getValue();

            // Date choice validity check
            if (fromLocalDate == null) return;
            if (toLocalDate == null) return;
            if (isInvalidDate()) return;
            
            // Clear line chart
            lineChart.getData().remove(0); 
            lineChart.setAnimated(true);

            // Setup the series
            XYChart.Series series = new XYChart.Series();
            series.setName(String.format("Expenses from %s until %s", fromLocalDate.toString(), toLocalDate.toString()));

            // Get total expenses per day, from start date to end date
            for (LocalDate date = fromLocalDate; date.isBefore(toLocalDate.plusDays(1)); date = date.plusDays(1)) {
                // Query statement
                String queryStatement;
                queryStatement = String.format("SELECT * FROM item WHERE date='%s'", date.toString());
                
                // Getting the data
                int totalExpenses = 0;
                try {
                    ResultSet rs;
                    rs = statement.executeQuery(queryStatement);
                    while (rs.next()) { // Iterate through all the data recieved
                        totalExpenses += rs.getInt("value");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // Add this date total expenses to the series
                series.getData().add(new XYChart.Data(date.toString(), totalExpenses));
            }

            // Add the new series to the line chart
            lineChart.getData().add(series);
            lineChart.setAnimated(false);
        }
        

        /**
         * Refresh Pie Chart
         */
        else if (pieChart.isVisible()) {
            // Get from and to dates
            LocalDate fromLocalDate = fromDatePick.getValue();
            LocalDate toLocalDate = toDatePick.getValue();

            // Date choice validity check
            if (fromLocalDate == null) return;
            if (toLocalDate == null) return;
            if (isInvalidDate()) return;

            // Check if data found or not
            boolean dataFound = false;

            // PieChart.Data array to store all expenses data per category
            PieChart.Data[] totalPerCategory = new PieChart.Data[categoryList.length];

            // Get total expenses per category between two dates
            for (int i = 0; i < categoryList.length; i++) {
                // Query statement
                String category = categoryList[i];
                String queryForCategory;
                queryForCategory = String.format("SELECT * FROM item WHERE (date BETWEEN '%s' AND '%s') AND (type='%s')", fromLocalDate.toString(), toLocalDate.toString(), category);

                // Getting the data
                int totalExpenses = 0;
                try {
                    ResultSet rs;
                    rs = statement.executeQuery(queryForCategory);
                    while (rs.next()) { // Iterate through all the data recieved
                        totalExpenses += rs.getInt("value");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // Add this category total expenses data to PieChart.Data array
                totalPerCategory[i] = new PieChart.Data(category, totalExpenses);
                if (totalExpenses != 0) { dataFound = true; } // Minimal one true value
            }

            // Generating the pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(totalPerCategory);
            pieChart.setData(pieChartData);
            pieChart.setTitle(String.format("Expenses from %s until %s", fromLocalDate.toString(), toLocalDate.toString()));
            if (dataFound) { noData.setVisible(false); } else { noData.setVisible(true); };
        }
    }
}