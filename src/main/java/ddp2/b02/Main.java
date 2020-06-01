package ddp2.b02;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    Scene dataInputScene;
    Scene compareScene;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        //Data Input Page
        FXMLLoader dataInputFxmlLoader = new FXMLLoader();
        dataInputFxmlLoader.setLocation(getClass().getResource("/DataInputPage.fxml"));
        Parent dataInputRoot = dataInputFxmlLoader.load();
        dataInputScene = new Scene(dataInputRoot,800,600);

        //Compare Page
        FXMLLoader comparePageFxmlLoader = new FXMLLoader();
        comparePageFxmlLoader.setLocation(getClass().getResource("/ComparePage.fxml"));
        Parent comparePageRoot = comparePageFxmlLoader.load();
        compareScene = new Scene(comparePageRoot,800,600);

        //Passing data to DataInputController
        ((DataInputPageController) dataInputFxmlLoader.getController()).setStage(primaryStage);
        ((DataInputPageController) dataInputFxmlLoader.getController()).getScene(compareScene);

        //Passing data to ComparePageController
        ((ComparePageController) comparePageFxmlLoader.getController()).setStage(primaryStage);
        ((ComparePageController) comparePageFxmlLoader.getController()).getScene(dataInputScene);

        //Set Scene
        primaryStage.setScene(dataInputScene);
        primaryStage.show();
    }
}
