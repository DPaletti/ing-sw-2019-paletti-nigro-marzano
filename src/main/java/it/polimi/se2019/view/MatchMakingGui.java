package it.polimi.se2019.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class MatchMakingGui extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MatchMakingGui.class.getClassLoader().getResource("fxml/match_making.fxml"));
        AnchorPane anchorPane = loader.load();

        Scene scene = new Scene(anchorPane);

        primaryStage.setTitle("Adrenaline: waiting for more players");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}



